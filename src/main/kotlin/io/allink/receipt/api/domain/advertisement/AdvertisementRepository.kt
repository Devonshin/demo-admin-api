package io.allink.receipt.api.domain.advertisement

import io.allink.receipt.api.repository.ExposedRepository
import io.allink.receipt.api.domain.merchant.MerchantTagTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import java.util.*

/**
 * Package: io.allink.receipt.admin.domain.admin
 * Created: Devonshin
 * Date: 13/04/2025
 */

interface AdvertisementRepository : ExposedRepository<AdvertisementTable, UUID, AdvertisementModel> {
}