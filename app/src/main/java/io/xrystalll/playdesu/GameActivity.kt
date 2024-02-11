package io.xrystalll.playdesu

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.ShaderConfig
import io.xrystalll.playdesu.util.DownloadHelper
import io.xrystalll.playdesu.util.SystemUIHelper
import java.io.File


class GameActivity : AppCompatActivity() {
    private lateinit var context: GameActivity
    private lateinit var retroView: GLRetroView
    private var gameLoaded = false
    private var startPressed = false
    private var selectPressed = false


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.context = this

        setContentView(R.layout.activity_game)

        SystemUIHelper(window).hideSystemUI()

        val titleName = findViewById<TextView>(R.id.title)
        val loading = findViewById<TextView>(R.id.loading)

        val extras = intent.extras
        if (extras != null) {
            val id = extras.getString("PROP_ID")
            val displayName = extras.getString("PROP_NAME")
            val gameSystem = extras.getString("PROP_SYSTEM")
            val rom = extras.getString("PROP_ROM")

            titleName.text = displayName

            val fileFormat = when (gameSystem) {
                "NES" -> ".nes"
                "SNES" -> ".snes"
                "SEGA" -> ".gen"
                "GBA" -> ".gba"
                "PSX" -> ".bin"
                else -> ""
            }

            if (fileFormat.isEmpty()) {
                finish()
            }

            val fileName = id + fileFormat

            val romFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            val core = when (gameSystem) {
                "NES" -> "libsnes9x_libretro_android.so"
                "SNES" -> "libpcsx_rearmed_libretro_android.so"
                "SEGA" -> "libgenesis_plus_gx_libretro_android.so"
                "GBA" -> "libmgba_libretro_android.so"
                "PSX" -> "libpcsx_rearmed_libretro_android.so"
                else -> ""
            }

            if (DownloadHelper.isLocalFileExists(fileName)) {
                runEmulator(core, romFile, titleName, loading)
            } else {
                loading.visibility = VISIBLE

                DownloadHelper.downloadFile(
                    fileName,
                    rom.toString(),
                    context,
                    onComplete = {
                        runEmulator(core, romFile, titleName, loading)
                    }
                )
            }
        }
    }

    private fun runEmulator(core: String, rom: File, titleName: TextView, loading: TextView) {
        val data = GLRetroViewData(this).apply {
            coreFilePath = core
            gameFilePath = rom.canonicalPath
            gameFileBytes = null
            systemDirectory = filesDir.absolutePath
            savesDirectory = filesDir.absolutePath
            variables = arrayOf()
            saveRAMState = null
            /*
             * (Optional) Shader to apply to the view.
             *
             * SHADER_DEFAULT:      Bilinear filtering, can cause fuzziness in retro games.
             * SHADER_CRT:          Classic CRT scan lines.
             * SHADER_LCD:          Grid layout, similar to Nintendo DS bottom screens.
             * SHADER_SHARP:        Raw, unfiltered image.
             * SHADER_UPSCALING:    Improve the quality of retro graphics.
             */
            shader = ShaderConfig.Default
            rumbleEventsEnabled = true
            preferLowLatencyAudio = true
        }

        retroView = GLRetroView(this, data)

        lifecycle.addObserver(retroView)

        gameLoaded = true
        titleName.visibility = GONE
        loading.visibility = GONE

        val frameLayout = findViewById<FrameLayout>(R.id.gamecontainer)

        frameLayout.addView(retroView)
        retroView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            sendMotionEvent(
                event,
                GLRetroView.MOTION_SOURCE_DPAD,
                MotionEvent.AXIS_HAT_X,
                MotionEvent.AXIS_HAT_Y,
                0
            )
            sendMotionEvent(
                event,
                GLRetroView.MOTION_SOURCE_ANALOG_LEFT,
                MotionEvent.AXIS_X,
                MotionEvent.AXIS_Y,
                0
            )
            sendMotionEvent(
                event,
                GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,
                MotionEvent.AXIS_Z,
                MotionEvent.AXIS_RZ,
                0
            )
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameLoaded) {
            retroView.sendKeyEvent(event.action, keyCode)
            return super.onKeyDown(keyCode, event)
        }

        return false
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (gameLoaded) {
            if (keyCode == KeyEvent.KEYCODE_BUTTON_START) {
                startPressed = true
            } else if (keyCode == KeyEvent.KEYCODE_BUTTON_SELECT) {
                selectPressed = true
            }

            if (startPressed and selectPressed) {
                startPressed = false
                selectPressed = false
                StartMenuDialogFragment().show(supportFragmentManager, "GAME_DIALOG")
            }

            retroView.sendKeyEvent(event.action, keyCode)
            return super.onKeyUp(keyCode, event)
        }

        return false
    }

    private fun sendMotionEvent(
        event: MotionEvent,
        source: Int,
        xAxis: Int,
        yAxis: Int,
        port: Int
    ) {
        if (gameLoaded) {
            retroView.sendMotionEvent(
                source,
                event.getAxisValue(xAxis),
                event.getAxisValue(yAxis),
                port
            )
        }
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
    )
    override fun onBackPressed() {
        if (gameLoaded) return

        super.onBackPressed()
    }

}


class StartMenuDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            SystemUIHelper(requireActivity().window).showSystemUI()

            val menuList = arrayOf("Resume", "Save", "Load", "Quit", "Restart")

            builder.setTitle("Game menu")
            builder.setItems(menuList) { _, which ->
                when (which) {
                    0 -> {
                        SystemUIHelper(requireActivity().window).hideSystemUI()
                    }
                    1 -> {
                        SystemUIHelper(requireActivity().window).hideSystemUI()
                    }
                    2 -> {
                        SystemUIHelper(requireActivity().window).hideSystemUI()
                    }
                    3 -> {
                        SystemUIHelper(requireActivity().window).hideSystemUI()
                        requireActivity().finish()
                    }
                    4 -> {
                        SystemUIHelper(requireActivity().window).hideSystemUI()
                    }
                }
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
