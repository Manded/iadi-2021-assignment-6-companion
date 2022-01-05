package pt.unl.fct.di.iadidemo.bookshelf.domain

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import javax.persistence.*


@Entity
data class BookDAO(
    @Id @GeneratedValue
    val id:Long,

    var title:String,

    @ManyToMany
    var authors:MutableList<AuthorDAO>,

    @OneToOne
    var image:ImageDAO
    )


@Entity
data class AuthorDAO(
    @Id @GeneratedValue
    val id:Long,

    val name:String,

    )

@Entity
data class ImageDAO(
    @Id @GeneratedValue
    val id:Long,

    val url:String,

    )

@Entity
data class UserDAO(
    @Id
    val username:String,

    var password:String,

    @ManyToMany(fetch = FetchType.EAGER)
    val roles:List<RoleDAO>,

    val name:String,

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch= FetchType.EAGER)
    val tokens: MutableList<TokenDAO>
    )

@Entity
data class RoleDAO(

    @Id
    val tag:String
)
@Entity
data class TokenDAO(
    @Id @GeneratedValue
    val id: Long,

    var token:String,

    @ManyToOne
    val owner:UserDAO
)