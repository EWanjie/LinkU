package com.example.myapplication

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import retrofit2.Response
import kotlin.math.abs
import retrofit2.Call
import retrofit2.Callback

class Aplikation : Fragment(R.layout.fragment_aplikation) {

    // ---- сохранение состояния карты ----
    private val MAP_PREFS = "map_state_prefs"
    private val KEY_LAT = "last_center_lat"
    private val KEY_LON = "last_center_lon"
    private val KEY_ZOOM = "last_zoom"

    private var map: MapView? = null
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private var compassOverlay: CompassOverlay? = null      // источник угла, НЕ добавляем в overlays
    private var compassArrow: ImageButton? = null

    // follow mode
    private var followMode = false
    private var pendingGpsPrompt = false

    // ориентация
    private var lastBearing: Float = 0f
    private var bearingInited = false
    private var suppressAutoRotateUntil = 0L

    private val FOLLOW_INTERVAL_MS = 200L
    private var followRunnable: Runnable? = null

    // пороги и сглаживание
    private val STILL_SPEED_MPS = 0.5f          // ниже — считаем, что стоим
    private val DEAD_BAND_DEG = 5f              // гистерезис «на месте»
    private val MAX_STEP_DEG = 20f              // макс. шаг поворота «на месте»
    private val MOVE_SNAP_DEG = 25f             // если разница больше — щёлкнем сразу

    // сенсоры (fallback для «на месте»)
    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null
    private var sensorListener: SensorEventListener? = null

    private var finishMarker: Marker? = null
    private var routeOverlay: Polyline? = null

    // permissions
    private val requestPerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val ok = (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true)
        if (ok) ensureReadyOrPromptGps()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map = view.findViewById(R.id.mapView)

        // Xiaomi/MIUI иногда «красит» виджеты при резком повороте полотна — переводим карту в софт-рендер только на их устройствах
        if (Build.MANUFACTURER.equals("Xiaomi", true)) {
            map?.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        map?.apply {

            setMultiTouchControls(true)
            setTileSource(TileSourceFactory.MAPNIK)
            setTilesScaledToDpi(true)
            maxZoomLevel = 21.0
            overlays.add(RotationGestureOverlay(this).apply { isEnabled = true })
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        }
        restoreMapStateOrDefault()

        // компас как ИСТОЧНИК азимута (НЕ добавляем в overlays, чтобы не рисовался)
        compassOverlay = CompassOverlay(
            requireContext(),
            InternalCompassOrientationProvider(requireContext()),
            map
        ).apply { enableCompass() }

        // зум
        view.findViewById<ImageButton>(R.id.btnZoomIn).setOnClickListener { map?.controller?.zoomIn() }
        view.findViewById<ImageButton>(R.id.btnZoomOut).setOnClickListener { map?.controller?.zoomOut() }

        // «Где я»: включаем follow, центрируем и задаём стартовый угол (мягкий подъём зума 18 -> 19)
        view.findViewById<ImageButton>(R.id.locationButton).setOnClickListener {
            if (!ensureReadyOrPromptGps()) return@setOnClickListener

            val m = map ?: return@setOnClickListener
            val loc = myLocationOverlay?.lastFix
            if (loc == null) {
                myLocationOverlay?.enableMyLocation()
                toast("Ждём GPS…")
                return@setOnClickListener
            }

            enableFollowMode()
            suppressAutoRotateFor(800)

            m.controller.setZoom(18.0)
            m.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
            m.postDelayed({ m.controller.setZoom(19.0) }, 350)

            val moving = loc.hasBearing() && loc.hasSpeed() && loc.speed > STILL_SPEED_MPS
            val initial = if (moving) loc.bearing else (compassOverlay?.orientation ?: lastBearing)
            m.mapOrientation = initial
            lastBearing = initial
            bearingInited = true

            startFollowTicker()
        }

        // «сброс поворота»
        compassArrow = view.findViewById(R.id.dellRotation)
        map?.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                compassArrow?.rotation = -(map?.mapOrientation ?: 0f)
                return true
            }
            override fun onZoom(event: ZoomEvent?): Boolean {
                compassArrow?.rotation = -(map?.mapOrientation ?: 0f)
                return true
            }
        })
        compassArrow?.setOnClickListener { resetRotation() }

        // касание карты — выключаем follow и временно гасим авто-поворот
        map?.setOnTouchListener { _, ev ->
            if (
                followMode && (
                        ev.actionMasked == MotionEvent.ACTION_DOWN ||
                                ev.actionMasked == MotionEvent.ACTION_POINTER_DOWN ||
                                ev.actionMasked == MotionEvent.ACTION_MOVE ||
                                ev.actionMasked == MotionEvent.ACTION_SCROLL
                        )
            ) {
                disableFollowMode()
                suppressAutoRotateFor(1500)
            }
            false
        }
        addTapOverlay()
        ensureReadyOrPromptGps()
    }

    private fun addTapOverlay() {
        val tapOverlay = MapEventsOverlay(requireContext(), object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                if (!ensureReadyOrPromptGps()) return true

                val cur = myLocationOverlay?.myLocation
                if (cur == null) {
                    toast("Не удалось получить текущее местоположение")
                    return true
                }
                val start = GeoPoint(cur.latitude, cur.longitude)
                finishMarker = placeOrMoveMarker(p, finishMarker, "Финиш")
                buildRouteGraphHopper(start, p)
                return true
            }

            override fun longPressHelper(p: GeoPoint) = false
        })
        map?.overlays?.add(0, tapOverlay)
    }

    private fun buildRouteGraphHopper(start: GeoPoint, end: GeoPoint) {
        val key = BuildConfig.GRAPHOPPER_API_KEY
        if (key.isBlank() || key.startsWith("YOUR_")) {
            toast("Нет ключа GraphHopper. Укажи GRAPHOPPER_API_KEY")
            return
        }

        routeOverlay?.let { map?.overlays?.remove(it) }
        routeOverlay = null
        map?.invalidate()

        val points = listOf(
            "${start.latitude},${start.longitude}",
            "${end.latitude},${end.longitude}"
        )

        Network.gh.route(
            points = points,
            vehicle = "car",
            locale = "ru",
            pointsEncoded = false,
            instructions = true,
            key = key
        )
            .enqueue(object : Callback<GHResponse> {
                override fun onResponse(call: Call<GHResponse>, response: Response<GHResponse>) {
                    val body = response.body()
                    if (!response.isSuccessful || body == null || body.paths.isEmpty()) {
                        toast("Маршрут не найден (${response.code()})")
                        return
                    }
                    val path = body.paths.first()
                    val coords = path.points.coordinates
                    if (coords.isEmpty()) {
                        toast("Пустая геометрия маршрута")
                        return
                    }

                    val pts = coords.mapNotNull { c -> if (c.size >= 2) GeoPoint(c[1], c[0]) else null }
                    routeOverlay = Polyline().apply {
                        setPoints(pts)
                        outlinePaint.apply {
                            color = Color.parseColor("#1976D2")  // синий цвет
                            strokeWidth = 12f                    // толщина линии
                            isAntiAlias = true                   // сглаживание краёв
                            strokeJoin = android.graphics.Paint.Join.ROUND
                            strokeCap = android.graphics.Paint.Cap.ROUND
                        }
                    }
                    map?.overlays?.add(0, routeOverlay)
                    map?.invalidate()
                }

                override fun onFailure(call: Call<GHResponse>, t: Throwable) {
                    toast("Ошибка сети/роутинга: ${t.localizedMessage ?: "неизвестно"}")
                }
            })
    }


    private fun placeOrMoveMarker(p: GeoPoint, current: Marker?, title: String): Marker {
        val m = map ?: throw IllegalStateException("MapView is null")
        val mk = current ?: Marker(m).also {
            it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            m.overlays.add(it)
        }
        mk.position = p
        mk.title = title
        m.invalidate()
        return mk
    }



    // ---------- Follow mode ----------
    private fun enableFollowMode() {
        followMode = true
        myLocationOverlay?.enableFollowLocation()
        map?.controller?.setZoom(19.0)
    }
    private fun disableFollowMode() {
        followMode = false
        myLocationOverlay?.disableFollowLocation()
        stopFollowTicker()
    }
    // ---------------------------------

    private fun suppressAutoRotateFor(ms: Long) {
        suppressAutoRotateUntil = System.currentTimeMillis() + ms
    }

    private fun startFollowTicker() {
        val m = map ?: return
        stopFollowTicker()

        followRunnable = object : Runnable {
            override fun run() {
                val loc = myLocationOverlay?.lastFix
                if (followMode && loc != null) {
                    // центр на пользователе
                    m.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))

                    // поворачиваем КАРТУ (маркер не трогаем!)
                    if (System.currentTimeMillis() >= suppressAutoRotateUntil) {
                        val moving = loc.hasBearing() && loc.hasSpeed() && loc.speed >= 1.0f
                        val target = if (moving) loc.bearing else (compassOverlay?.orientation ?: lastBearing)
                        val cur = m.mapOrientation

                        val next = if (moving) {
                            // двигаемся: быстро догоняем курс
                            val diff = absAngleDiff(cur, target)
                            if (diff > MOVE_SNAP_DEG) target else lerpAngle(cur, target, 0.6f)
                        } else {
                            // стоим: мягко, с deadband и ограничением шага
                            smoothStill(cur, target)
                        }

                        m.mapOrientation = next
                        lastBearing = next
                        bearingInited = true
                    }
                }
                m.postDelayed(this, FOLLOW_INTERVAL_MS)
            }
        }.also { m.post(it) }
    }
    private fun stopFollowTicker() {
        followRunnable?.let { map?.removeCallbacks(it) }
        followRunnable = null
    }

    // --- сенсоры (для «на месте») ---
    private fun startHeadingSensors() {
        if (sensorManager == null)
            sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (rotationSensor == null)
            rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensorListener != null || rotationSensor == null) return   // FIX: логическое ИЛИ

        sensorListener = object : SensorEventListener {
            private val rot = FloatArray(9)
            private val ori = FloatArray(3)
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
                val m = map ?: return
                if (!followMode) return

                val spd = myLocationOverlay?.lastFix?.speed ?: 0f
                if (spd > STILL_SPEED_MPS) return // в движении рулит GPS

                try {
                    SensorManager.getRotationMatrixFromVector(rot, event.values)
                    SensorManager.getOrientation(rot, ori)
                    var az = Math.toDegrees(ori[0].toDouble()).toFloat()
                    if (az < 0) az += 360f
                    val next = smoothStill(m.mapOrientation, az)
                    m.mapOrientation = next
                    lastBearing = next
                    bearingInited = true
                } catch (_: Throwable) {}
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager?.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
    }
    private fun stopHeadingSensors() {
        sensorListener?.let { sensorManager?.unregisterListener(it) }
        sensorListener = null
    }

    // ---- углы ----
    private fun normAngle(a: Float): Float = ((a % 360f) + 360f) % 360f
    private fun angleDiff(from: Float, to: Float): Float {
        return (to - from + 540f) % 360f - 180f
    }
    private fun absAngleDiff(from: Float, to: Float): Float = abs(angleDiff(from, to))
    private fun lerpAngle(from: Float, to: Float, alpha: Float): Float {
        val d = angleDiff(from, to)
        return normAngle(from + alpha * d)

    }
    private fun smoothStill(cur: Float, target: Float): Float {
        if (!bearingInited) return target
        var d = angleDiff(cur, target)
        if (abs(d) < DEAD_BAND_DEG) return cur
        d = d.coerceIn(-MAX_STEP_DEG, MAX_STEP_DEG)
        return normAngle(cur + 0.2f * d)
    }

    // сброс поворота
    private fun resetRotation() {
        val m = map ?: return
        val start = m.mapOrientation
        ValueAnimator.ofFloat(start, 0f).apply {
            duration = 250
            addUpdateListener { anim ->
                val angle = anim.animatedValue as Float
                m.mapOrientation = angle
                compassArrow?.rotation = -angle
            }
            start()
        }
    }

    // ----- разрешения / GPS / overlay -----
    private fun hasFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun isGpsEnabled(): Boolean {
        val lm = requireContext().getSystemService(LocationManager::class.java)
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun openGpsSettings() {
        pendingGpsPrompt = true
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private fun enableMyLocationOverlaySilently() {
        val mapView = map ?: return
        if (myLocationOverlay == null) {
            val provider = GpsMyLocationProvider(requireContext().applicationContext).apply {
                locationUpdateMinTime = 200L
                locationUpdateMinDistance = 0f
            }
            myLocationOverlay = MyLocationNewOverlay(provider, mapView).apply {
                enableMyLocation()
                isDrawAccuracyEnabled = true
                // Иконки — дефолтные; маркер вращается вместе с полотном карты
            }
            mapView.overlays.add(myLocationOverlay)
            mapView.invalidate()
        }
    }

    private fun ensureReadyOrPromptGps(): Boolean {
        if (!hasFineLocationPermission()) {
            requestPerms.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            return false
        }
        if (!isGpsEnabled()) {
            openGpsSettings()
            return false
        }
        enableMyLocationOverlaySilently()
        return true
    }

    // ---- сохранение/восстановление состояния карты ----
    private fun saveMapState() {
        val m = map ?: return
        val center = m.mapCenter
        val zoom = m.zoomLevelDouble
        val prefs = requireContext().getSharedPreferences(MAP_PREFS, 0)
        prefs.edit {
            putFloat(KEY_LAT, center.latitude.toFloat())
            putFloat(KEY_LON, center.longitude.toFloat())
            putFloat(KEY_ZOOM, zoom.toFloat())
        }
    }
    private fun restoreMapStateOrDefault() {
        val m = map ?: return
        val prefs = requireContext().getSharedPreferences(MAP_PREFS, 0)
        if (prefs.contains(KEY_LAT) && prefs.contains(KEY_LON) && prefs.contains(KEY_ZOOM)) {
            val lat = prefs.getFloat(KEY_LAT, 0f).toDouble()
            val lon = prefs.getFloat(KEY_LON, 0f).toDouble()
            val zoom = prefs.getFloat(KEY_ZOOM, 15.0f).toDouble()
            m.controller.setZoom(zoom)
            m.controller.setCenter(GeoPoint(lat, lon))
        } else {
            m.controller.setZoom(19.0)
            m.controller.setCenter(GeoPoint(55.751999, 37.617734))
        }
    }

    // ---- lifecycle ----
    override fun onResume() {
        super.onResume()
        map?.onResume()

        requireActivity().enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)

        val root = requireView()
        ViewCompat.setOnApplyWindowInsetsListener(root, null)
        root.setPadding(0, 0, 0, 0)
        ViewCompat.requestApplyInsets(root)

        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        ).isAppearanceLightStatusBars = true

        if (pendingGpsPrompt) {
            if (!isGpsEnabled()) toast("GPS выключен") else enableMyLocationOverlaySilently()
            pendingGpsPrompt = false
        }

        startHeadingSensors()
    }

    override fun onPause() {
        saveMapState()
        stopFollowTicker()
        stopHeadingSensors()
        myLocationOverlay?.disableMyLocation()
        map?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        stopFollowTicker()
        stopHeadingSensors()
        myLocationOverlay = null
        map = null
        super.onDestroyView()
    }

    private fun toast(msg: String) =
        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
}