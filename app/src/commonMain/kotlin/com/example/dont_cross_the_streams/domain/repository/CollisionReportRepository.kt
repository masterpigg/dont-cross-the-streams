package com.example.dont_cross_the_streams.domain.repository

import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.CollisionReport

interface CollisionReportRepository {
    /** Individual collision/carcass reports within [bounds]; null when the source is unreachable. */
    suspend fun getCollisionReports(bounds: BoundingBox): List<CollisionReport>?
}
