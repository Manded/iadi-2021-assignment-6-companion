package pt.unl.fct.di.iadidemo.bookshelf.application.services

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import pt.unl.fct.di.iadidemo.bookshelf.domain.UserDAO
import pt.unl.fct.di.iadidemo.bookshelf.domain.UserRepository
import pt.unl.fct.di.iadidemo.bookshelf.domain.TokenDAO
import pt.unl.fct.di.iadidemo.bookshelf.domain.TokenRepository
import java.util.*

@Service
class UserService(val users: UserRepository, val tokens: TokenRepository) {

    fun findUser(username:String) = users.findById(username)

    fun addUser(user: UserDAO) : Optional<UserDAO> {
        val aUser = users.findById(user.username)

        return if ( aUser.isPresent )
            Optional.empty()
        else {
            user.password = BCryptPasswordEncoder().encode(user.password)
            Optional.of(users.save(user))
        }
    }

    fun addToken(user: String, token: String) : Optional<UserDAO> {
        var user = findUser(user)
        user.ifPresent {
            val userdao = user.get()
            val tokendao = TokenDAO(0,token, userdao)
            //userdao.tokens.add(tokendao)
            //users.save(userdao)
            tokens.save(tokendao)
        }
        return user
    }

    fun refreshToken(user: String, token: String) : Optional<TokenDAO>{
        var useropt = findUser(user)
        useropt.ifPresent {
            val tokendao = tokens.getTokenByUser(user,token).ifPresent{
                it.token = token
                tokens.save(it)
            }
        }
        return Optional.empty()
    }

    fun deleteUserTokens(user:String): Optional<TokenDAO>{
        tokens.deleteTokensByUser(user)
        return Optional.empty()
    }

}
