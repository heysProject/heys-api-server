package com.api.heys.entity

import org.hibernate.annotations.Fetch
import javax.persistence.*

/**
 * 컨텐츠 조회 테이블
 */

@Entity
@Table(name = "content_view")
class ContentView(
        contents: Contents,
): BaseIdentity() {
    @Column(name = "count")
    var count: Long = 0

    @OneToOne
    @JoinColumn(name = "content_id")
    var contents: Contents = contents

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var viewers: MutableSet<Users> = mutableSetOf()
}