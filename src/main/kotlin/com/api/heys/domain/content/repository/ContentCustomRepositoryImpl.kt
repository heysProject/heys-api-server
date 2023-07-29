package com.api.heys.domain.content.repository

import com.api.heys.constants.DefaultString
import com.api.heys.constants.MessageString
import com.api.heys.constants.enums.ContentOrderType
import com.api.heys.domain.content.dto.ExtraContentListItemData
import com.api.heys.domain.content.dto.GetExtraContentsParam
import com.api.heys.domain.content.dto.GetExtraContentsResponse
import com.api.heys.entity.*
import com.api.heys.helpers.DateHelpers
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ContentCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ContentCustomRepository {
    val qUsers: QUsers = QUsers.users
    val qContents: QContents = QContents.contents
    val qExtraContentDetail: QExtraContentDetail = QExtraContentDetail.extraContentDetail
    val qContentView: QContentView = QContentView.contentView
    val qInterest: QInterest = QInterest.interest
    val qInterestRelations: QInterestRelations = QInterestRelations.interestRelations
    val qChannels: QChannels = QChannels.channels
    val qContentBookMark: QContentBookMark = QContentBookMark.contentBookMark

    fun extraContentFilterQuery(
        queryBase: JPAQuery<Contents>,
        params: GetExtraContentsParam,
        role: String
    ): List<Contents> {
        var query = queryBase
            .join(qContents.extraDetail, qExtraContentDetail).fetchJoin()
            .leftJoin(qContents.channels, qChannels).fetchJoin()
            .leftJoin(qContents.contentViews, qContentView).fetchJoin()
            .leftJoin(qExtraContentDetail.interestRelations, qInterestRelations).fetchJoin()
            .leftJoin(qInterestRelations.interest, qInterest).fetchJoin()
            .where(qContents.removedAt.isNull.and(qContents.contentType.eq(params.type)))

        if (role != DefaultString.adminRole) {
            query = query.where(qContents.publishedAt.isNotNull)
        }

        // includeClosed filter (마감된 컨텐츠 포함 여부 - 조건 : DDay 남은것, 정원 안찬것)
        // includeClosed == true 이면 '마감 일자' 및 '제한 인원' 쿼리 무시
        // includeClosed == false 이거나 null 이면
        // '오늘 이후에 마감일자' 혹은 '오늘 이후 & 모집 기간 이내 마감일자' 및 '제한 인원수 보다 작은 것'만 쿼리
        if (params.includeClosed == null || params.includeClosed == false) {
            query = query.where(
                qExtraContentDetail.endDate.after(LocalDateTime.now())
            )

            // 마감 일자 값이 존재할 경우 해당 값 이전 요소만 쿼리
            if (!params.lastRecruitDate.isNullOrBlank()) {
                query = query.where(
                    qExtraContentDetail.endDate.before(LocalDateTime.parse(params.lastRecruitDate))
                )
            }
        }

        // 관심분야 파라미터 배열 요소중 하나라도 맞는게 있으면 쿼리 대상
        if (!params.interests.isNullOrEmpty()) {
            query = query.where(qInterest.name.`in`(params.interests))
        }

        if (params.order == ContentOrderType.Default) {
            query = query.orderBy(qContents.id.desc())
        } else if (params.order == ContentOrderType.Dday && !params.lastRecruitDate.isNullOrBlank()) {
            val dateDiff: NumberExpression<Int> =
                Expressions.numberTemplate(
                    Int::class.java,
                    "DATEDIFF({0}, {1})",
                    LocalDateTime.parse(params.lastRecruitDate),
                    qExtraContentDetail.endDate
                )
            query = query.orderBy(dateDiff.asc())
        } else if (params.order == ContentOrderType.Popular) {
            query = query.orderBy(qContentView.count().desc())
        }

        return query
            .limit(params.limit)
            .offset((params.page - 1) * params.limit)
            .distinct()
            .fetch()
    }

    fun extraContentFilterCountQuery(queryBase: JPAQuery<Long>, params: GetExtraContentsParam, role: String): Long {
        var query = queryBase
            .join(qContents.extraDetail, qExtraContentDetail)
            .leftJoin(qContents.channels, qChannels)
            .leftJoin(qContents.contentViews, qContentView)
            .leftJoin(qExtraContentDetail.interestRelations, qInterestRelations)
            .leftJoin(qInterestRelations.interest, qInterest)
            .where(qContents.removedAt.isNull.and(qContents.contentType.eq(params.type)))

        if (role != DefaultString.adminRole) {
            query = query.where(qContents.publishedAt.isNotNull)
        }

        // includeClosed filter (마감된 컨텐츠 포함 여부 - 조건 : DDay 남은것, 정원 안찬것)
        // includeClosed == true 이면 '마감 일자' 및 '제한 인원' 쿼리 무시
        // includeClosed == false 이거나 null 이면
        // '오늘 이후에 마감일자' 혹은 '오늘 이후 & 모집 기간 이내 마감일자' 및 '제한 인원수 보다 작은 것'만 쿼리
        if (params.includeClosed == null || params.includeClosed == false) {
            query = query.where(
                qExtraContentDetail.endDate.after(LocalDateTime.now())
            )

            // 마감 일자 값이 존재할 경우 해당 값 이전 요소만 쿼리
            if (!params.lastRecruitDate.isNullOrBlank()) {
                query = query.where(
                    qExtraContentDetail.endDate.before(LocalDateTime.parse(params.lastRecruitDate))
                )
            }
        }

        // 관심분야 파라미터 배열 요소중 하나라도 맞는게 있으면 쿼리 대상
        if (!params.interests.isNullOrEmpty()) {
            query = query.where(qInterest.name.`in`(params.interests))
        }

        return query.fetchOne() ?: 0
    }

    /**
     * Filter conditions
     *
     * interest - 관심분야 리스트
     * lastRecruitDate - 마감 일자(모집기간) 프론트엔드 에서 날짜를 보내주면, 현재 날짜에서 diff 연산하여 탐색.
     * */
    override fun findExtraContents(params: GetExtraContentsParam, role: String): GetExtraContentsResponse {
        val query = jpaQueryFactory.selectFrom(qContents)
        val totalCountQuery = jpaQueryFactory.select(qContents.countDistinct()).from(qContents)

        val data = extraContentFilterQuery(query, params, role).map {
            val detail = it.extraDetail!!
            val view = it.contentViews
            val channels = it.channels
            ExtraContentListItemData(
                id = it.id,
                title = detail.title,
                company = detail.company,
                viewCount = view.count().toLong(),
                channelCount = channels.count(),
                dDay = DateHelpers.calculateDday(detail.endDate),
                previewImgUri = detail.previewImgUri,
                publishedAt = it.publishedAt,
            )
        }

        val totalCount = extraContentFilterCountQuery(totalCountQuery, params, role)
        val totalPage = DateHelpers.calcTotalPage(totalCount, params.limit)

        return GetExtraContentsResponse(data, totalPage, MessageString.SUCCESS_EN)
    }

    override fun getExtraContent(contentId: Long): Contents? {
        return jpaQueryFactory
            .selectFrom(qContents)
            .join(qContents.extraDetail, qExtraContentDetail).fetchJoin()
            .leftJoin(qContents.contentBookMarks, qContentBookMark).fetchJoin()
            .leftJoin(qContents.contentViews, qContentView).fetchJoin()
            .leftJoin(qContents.channels, qChannels).fetchJoin()
            .leftJoin(qExtraContentDetail.interestRelations, qInterestRelations).fetchJoin()
            .where(qContents.removedAt.isNull.and(qContents.id.eq(contentId)))
            .fetchOne()
    }

    override fun getContentView(contentId: Long, userId: Long): ContentView? {
        val query = jpaQueryFactory
            .selectFrom(qContentView)
            .join(qContentView.content, qContents).fetchJoin()
            .join(qContentView.users, qUsers).fetchJoin()
            .where(qContents.id.eq(contentId).and(qUsers.id.eq(userId)))

        return query.fetchOne()
    }

    override fun getContentBookMark(contentId: Long, userId: Long): ContentBookMark? {
        val query = jpaQueryFactory
            .selectFrom(qContentBookMark)
            .join(qContentBookMark.content, qContents).fetchJoin()
            .join(qContentBookMark.users, qUsers).fetchJoin()
            .where(qContents.id.eq(contentId))
            .where(qUsers.id.eq(userId))

        return query.fetchOne()
    }
}