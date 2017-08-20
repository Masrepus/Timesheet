package com.masrepus.timesheet.database

import java.util.*

/**
 * Created by samuel on 09.08.17.
 */
class References {
    companion object {
        const val TIMESHEETS = "timesheets"
        const val TIMERECORDS = "timerecords"
        const val USERS = "users"
    }
}

data class Timerecord(val start: Long = 0, val end: Long = 0, val salaryPerHour: Double = 0.0, val comment: String = "")

data class Timesheet(val name : String = "", val users: List<String> = LinkedList<String>())

data class User(val timesheets : List<String> = LinkedList())