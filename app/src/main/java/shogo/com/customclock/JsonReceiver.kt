package shogo.com.customclock

import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket

class JsonReceiver : Runnable {
	override fun run() {
		val socket = DatagramSocket(11111)
		while (true) {
			val data = DatagramPacket(
					kotlin.ByteArray(1024),
					kotlin.ByteArray(1024).size)
			socket.receive(data)
			if (String(data.data).startsWith("find")) {
				socket.send(data)
//				Log.d("address",data.address)
				println(data.address)
			} else {
				val json = JSONObject(String(data.data))
				println(json.toString())
				val hour = json.getInt("hour")
				val minute = json.getInt("minute")
				val second = json.getInt("second")
				val newClock = ClockTime(hour, minute, second)
				MainActivity.clock = newClock
				MainActivity.speed = json.getLong("speed")
				MainActivity.show = json.getBoolean("show")
				MainActivity.update = true
			}
		}
	}
}
