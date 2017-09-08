package shogo.com.customclock


class ClockTime(var hour: Int, var minute: Int, var second: Int) : Comparable<ClockTime> {
	override fun compareTo(other: ClockTime): Int {
		var delta = (other.hour - hour) * 3600
		delta += (other.minute - minute) * 60
		delta += other.second - second
		return delta
	}

	fun add1() {
		second++
		if (second >= 60) {
			second = 0
			minute++
			if (minute >= 60) {
				minute = 0
				hour++
				if (hour >= 24) {
					hour = 0
				}
			}
		}
	}

	fun sub1() {
		second--
		if (second < 0) {
			second = 59
			minute--
			if (minute < 0) {
				minute = 59
				hour--
				if (hour < 0) {
					hour = 23
				}
			}
		}
	}

	fun hour_minute_toString(): String {
		val h = if (hour < 10) "!$hour" else hour.toString()
		val m = if (minute < 10) "0$minute" else minute.toString()
		return "$h:$m"
	}

	fun second_toString(): String {
		return if (second < 10) "0$second" else second.toString()
	}

	override fun toString(): String {
		return "${hour_minute_toString()}:${second_toString()}"
	}
}