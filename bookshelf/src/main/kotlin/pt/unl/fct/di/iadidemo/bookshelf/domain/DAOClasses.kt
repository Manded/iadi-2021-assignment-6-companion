package pt.unl.fct.di.iadidemo.bookshelf.domain

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import javax.persistence.*


@Entity
data class BookDAO(
    @Id @GeneratedValue
    val id:Long,

    var title:String,

    @ManyToMany
    var authors:MutableList<AuthorDAO>,

    var image:String
    )


@Entity
data class AuthorDAO(
    @Id @GeneratedValue
    val id:Long,

    val name:String,

    )

@Entity
data class UserDAO(
    @Id
    val username:String,

    var password:String,

    @ManyToMany(fetch = FetchType.EAGER)
    val roles:List<RoleDAO>,

    val name:String,

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany
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

    @Column( length = 300 )
    var token:String,

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne
    val owner:UserDAO
)