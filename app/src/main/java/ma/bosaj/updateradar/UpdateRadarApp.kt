package ma.bosaj.updateradar

import android.app.Application
import ma.bosaj.updateradar.work.Reminders

class UpdateRadarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Reminders.createChannel(this)
    }
}
