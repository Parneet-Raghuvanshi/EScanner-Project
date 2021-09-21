package com.example.escannrr.uipr.processor

import org.opencv.core.Point
import org.opencv.core.Size

data class Corners(val corners: List<Point?>, val size: Size)