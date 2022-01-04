package pt.unl.fct.di.iadidemo.bookshelf

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.unl.fct.di.iadidemo.bookshelf.domain.*
import javax.transaction.Transactional

@SpringBootApplication
class SecurityApplication(
    val books:BookRepository,
    val users:UserRepository,
    val roles:RoleRepository,
    val authors:AuthorRepository,
    val images:ImageRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {

        val r1 = RoleDAO("ADMIN")
        val r2 = RoleDAO("REVIEWER")
        val r3 = RoleDAO("USER")
        roles.saveAll(listOf(r1, r2, r3))

        val u1 = UserDAO("user1",BCryptPasswordEncoder().encode("password1"),listOf(r3,r2),"User 1", mutableListOf())
        users.save(u1)

        val u2 = UserDAO("admin1",BCryptPasswordEncoder().encode("password1"), listOf(r1),"Admin 1", mutableListOf())
        users.save(u2)

        val a1 = AuthorDAO(0,"Philip K. Dick")
        authors.save(a1)

        val i1 = ImageDAO(0, "https://covers.openlibrary.org/b/id/9251896-L.jpg")
        val i2 = ImageDAO(0, "https://covers.openlibrary.org/b/id/11153217-L.jpg")
        val i3 = ImageDAO(0, "https://covers.openlibrary.org/b/id/10045188-L.jpg")
        images.saveAll(listOf(i1,i2,i3))

        val b1 = BookDAO(0,"Ubik", mutableListOf(a1), i1)
        val b2 = BookDAO(0,"Do Androids Dream of Electric Sheep?", mutableListOf(a1), i2)
        val b3 = BookDAO(0,"The Man in the High Castle", mutableListOf(a1), i3)

        books.saveAll(listOf(b1,b2,b3))
    }

}

fun main(args: Array<String>) {
    runApplication<SecurityApplication>(*args)
}
