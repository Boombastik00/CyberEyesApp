package com.cybereyes

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var face: View
    private lateinit var mouth: TextView
    private lateinit var leftEye: View
    private lateinit var rightEye: View
    private lateinit var leftPupil: View
    private lateinit var rightPupil: View
    private lateinit var leftBrow: View
    private lateinit var rightBrow: View
    private lateinit var leftBorder: View
    private lateinit var rightBorder: View

    private var isReacting = false
    private var touchJob: Job? = null
    private var shakeJob: Job? = null

    private var clickCount = 0
    private var lastClickTime = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()
        setContentView(R.layout.activity_main)

        face = findViewById(R.id.face_container)
        mouth = findViewById(R.id.mouth)
        leftEye = findViewById(R.id.left_eye)
        rightEye = findViewById(R.id.right_eye)
        leftPupil = findViewById(R.id.left_pupil)
        rightPupil = findViewById(R.id.right_pupil)
        leftBrow = findViewById(R.id.left_brow)
        rightBrow = findViewById(R.id.right_brow)
        leftBorder = findViewById(R.id.left_eye_border)
        rightBorder = findViewById(R.id.right_eye_border)

        var touchStartTime = 0L

        findViewById<View>(android.R.id.content).setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStartTime = System.currentTimeMillis()
                    touchJob = lifecycleScope.launch {
                        delay(1200)
                        startAnnoyedState()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val duration = System.currentTimeMillis() - touchStartTime
                    touchJob?.cancel()

                    if (!isReacting) {
                        if (duration < 300) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastClickTime <= 500) {
                                clickCount++
                            } else {
                                clickCount = 1
                            }
                            lastClickTime = currentTime

                            if (clickCount >= 5) {
                                lifecycleScope.launch {
                                    runDizzyEffect()
                                }
                                clickCount = 0
                            } else {
                                runSurpriseEffect()
                            }
                        }
                    } else {
                        stopAnnoyedState()
                    }
                }
            }
            true
        }

        lifecycleScope.launch {
            while (true) {
                if (!isReacting) {
                    val targetX = Random.nextInt(-250, 250).toFloat()
                    val targetY = Random.nextInt(-150, 150).toFloat()
                    val moveDuration = Random.nextLong(2500, 4000)

                    val pX = (targetX / 250) * 15f
                    val pY = (targetY / 150) * 10f

                    leftPupil.animate().translationX(pX).translationY(pY).setDuration(moveDuration / 2).start()
                    rightPupil.animate().translationX(pX).translationY(pY).setDuration(moveDuration / 2).start()

                    delay(100)
                    face.animate().translationX(targetX).translationY(targetY).setDuration(moveDuration).start()
                    delay(moveDuration + Random.nextLong(2000, 5000))

                    if (Random.nextInt(100) < 60 && !isReacting) {
                        smoothBlink(leftEye, rightEye)
                    }
                }
                delay(100)
            }
        }
    }

    private suspend fun runDizzyEffect() {
        isReacting = true
        mouth.text = "~"

        face.animate().cancel()

        val startTime = System.currentTimeMillis()
        val duration = 3000L

        while (System.currentTimeMillis() - startTime < duration) {
            val angle = (System.currentTimeMillis() - startTime).toDouble() / 120.0
            val radius = 10f * sin(angle / 5.0).toFloat().coerceAtLeast(5f)

            val offsetX = (radius * cos(angle)).toFloat()
            val offsetY = (radius * sin(angle)).toFloat()

            leftPupil.translationX = offsetX
            leftPupil.translationY = offsetY
            rightPupil.translationX = offsetX
            rightPupil.translationY = offsetY

            if (mouth.text != "~") mouth.text = "~"

            delay(16)
        }

        delay(50)

        stopAnnoyedState()
    }

    private fun runSurpriseEffect() {
        lifecycleScope.launch {
            isReacting = true
            leftBrow.animate().translationY(-15f).setDuration(150).start()
            rightBrow.animate().translationY(-15f).setDuration(150).start()
            leftBorder.animate().scaleX(1.15f).scaleY(1.25f).setDuration(150).start()
            rightBorder.animate().scaleX(1.15f).scaleY(1.25f).setDuration(150).start()
            mouth.text = "o"

            repeat(2) {
                leftEye.animate().scaleY(0.01f).setDuration(80).start()
                rightEye.animate().scaleY(0.01f).setDuration(80).start()
                delay(100)
                leftEye.animate().scaleY(1f).setDuration(80).start()
                rightEye.animate().scaleY(1f).setDuration(80).start()
                delay(100)
            }
            delay(1500)
            stopAnnoyedState()
        }
    }

    private fun startAnnoyedState() {
        isReacting = true
        mouth.text = "o"
        leftEye.animate().scaleY(0.1f).setDuration(100).start()
        rightEye.animate().scaleY(0.1f).setDuration(100).start()
        leftBrow.animate().rotation(25f).translationY(8f).setDuration(100).start()
        rightBrow.animate().rotation(-25f).translationY(8f).setDuration(100).start()
        leftBorder.animate().rotation(15f).setDuration(100).start()
        rightBorder.animate().rotation(-15f).setDuration(100).start()

        shakeJob = lifecycleScope.launch {
            while (isActive) {
                face.animate().translationXBy(20f).setDuration(40).start()
                delay(40)
                face.animate().translationXBy(-40f).setDuration(80).start()
                delay(80)
                face.animate().translationXBy(20f).setDuration(40).start()
                delay(40)
            }
        }
    }

    private fun stopAnnoyedState() {
        shakeJob?.cancel()
        mouth.text = "^"
        leftEye.animate().scaleY(1f).setDuration(300).start()
        rightEye.animate().scaleY(1f).setDuration(300).start()
        leftBrow.animate().rotation(-10f).translationY(0f).setDuration(300).start()
        rightBrow.animate().rotation(10f).translationY(0f).setDuration(300).start()
        leftBorder.animate().rotation(-5f).scaleX(1f).scaleY(1f).setDuration(300).start()
        rightBorder.animate().rotation(5f).scaleX(1f).scaleY(1f).setDuration(300).start()
        isReacting = false
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private suspend fun smoothBlink(eye1: View, eye2: View) {
        val d = 150L
        eye1.animate().scaleY(0.01f).setDuration(d).start()
        eye2.animate().scaleY(0.01f).setDuration(d).start()
        delay(d + 50)
        eye1.animate().scaleY(1f).setDuration(d).start()
        eye2.animate().scaleY(1f).setDuration(d).start()
    }
}