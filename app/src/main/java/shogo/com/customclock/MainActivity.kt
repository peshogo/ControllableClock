package shogo.com.customclock

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

	lateinit var hour_minute: TextView
	lateinit var second: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

		hour_minute = findViewById(R.id.hour_minute)
		second = findViewById(R.id.second)

		val typeface = Typeface.createFromAsset(assets, "DSEG7Modern-Bold.ttf")

		hour_minute.typeface = typeface
		second.typeface = typeface

		hour_minute.setOnClickListener {
			Toast.makeText(this, getLocalHostLANAddress().toString(), Toast.LENGTH_LONG).show()
		}
	}

	override fun onResume() {
		super.onResume()

		MainActivity.stop = false

		val cal = Calendar.getInstance()
		val clock = ClockTime(
				cal[Calendar.HOUR_OF_DAY],
				cal[Calendar.MINUTE],
				cal[Calendar.SECOND])

		hour_minute.text = clock.hour_minute_toString()
		second.text = clock.second_toString()

		val handler = Handler()

		thread {
			while (true) {
				if (MainActivity.stop) {
					break
				}
				val t = measureTimeMillis {
					if (MainActivity.update) {
						if (MainActivity.zoom) {
							TODO()
						} else {
							clock.hour = MainActivity.clock.hour
							clock.minute = MainActivity.clock.minute
							clock.second = MainActivity.clock.second
						}
						MainActivity.update = false
					}
					if (speed > 0) {
						clock.add1()
					} else if (speed < 0) {
						clock.sub1()
					}
					handler.post {
						if (MainActivity.show) {
							hour_minute.setTextColor(Color.WHITE)
							second.setTextColor(Color.WHITE)
						} else {
							hour_minute.setTextColor(Color.BLACK)
							second.setTextColor(Color.BLACK)
						}
						hour_minute.text = clock.hour_minute_toString()
						second.text = clock.second_toString()
					}
				}
				sleep(maxOf(Math.abs(speed) - t, 0L))
			}
		}

		Thread(JsonReceiver()).start()

	}

	override fun onPause() {
		super.onPause()
		MainActivity.stop = true

		finish()

	}

	companion object {
		var update = false
		var clock = ClockTime(0, 0, 0)
		var zoom = false
		var stop = false
		var show = true
		var speed = 1000L
	}

	fun sleep(t: Long) {
		try {
			Thread.sleep(t)
		} catch (e: InterruptedException) {
			e.printStackTrace()
		}
	}

	private fun getLocalHostLANAddress(): InetAddress {
		try {
			var candidateAddress: InetAddress? = null
			// Iterate all NICs (network interface cards)...
			val ifaces = NetworkInterface.getNetworkInterfaces()
			while (ifaces.hasMoreElements()) {
				val iface = ifaces.nextElement() as NetworkInterface
				// Iterate all IP addresses assigned to each card...
				val inetAddrs = iface.inetAddresses
				while (inetAddrs.hasMoreElements()) {
					val inetAddr = inetAddrs.nextElement() as InetAddress
					if (!inetAddr.isLoopbackAddress) {

						if (inetAddr.isSiteLocalAddress) {
							// Found non-loopback site-local address. Return it immediately...
							return inetAddr
						} else if (candidateAddress == null) {
							// Found non-loopback address, but not necessarily site-local.
							// Store it as a candidate to be returned if site-local address is not subsequently found...
							candidateAddress = inetAddr
							// Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
							// only the first. For subsequent iterations, candidate will be non-null.
						}
					}
				}
			}
			if (candidateAddress != null) {
				// We did not find a site-local address, but we found some other non-loopback address.
				// Server might have a non-site-local address assigned to its NIC (or it might be running
				// IPv6 which deprecates the "site-local" concept).
				// Return this non-loopback candidate address...
				return candidateAddress
			}
			// At this point, we did not find a non-loopback address.
			// Fall back to returning whatever InetAddress.getLocalHost() returns...
			val jdkSuppliedAddress = InetAddress.getLocalHost() ?: throw UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.")
			return jdkSuppliedAddress
		} catch (e: Exception) {
			val unknownHostException = UnknownHostException("Failed to determine LAN address: " + e)
			unknownHostException.initCause(e)
			throw unknownHostException
		}

	}
}