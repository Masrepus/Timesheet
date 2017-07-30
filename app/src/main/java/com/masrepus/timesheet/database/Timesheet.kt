package com.masrepus.timesheet.database

import java.util.*

/**
 * Created by samuel on 30.07.17.
 */
data class Timesheet(val name : String = "", val users: List<String> = LinkedList<String>())
