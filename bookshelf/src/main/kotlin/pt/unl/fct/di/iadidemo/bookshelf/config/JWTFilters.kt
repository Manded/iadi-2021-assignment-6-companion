package pt.unl.fct.di.iadidemo.bookshelf.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.web.filter.GenericFilterBean
import pt.unl.fct.di.iadidemo.bookshelf.domain.RoleDAO
import pt.unl.fct.di.iadidemo.bookshelf.domain.UserDAO
import pt.unl.fct.di.iadidemo.bookshelf.application.services.UserService
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.HashMap

object JWTSecret {
    private const val passphrase = "este é um grande segredo que tem que ser mantido escondido"
    val KEY: String = Base64.getEncoder().encodeToString(passphrase.toByteArray())
    const val SUBJECT = "JSON Web Token for CIAI 2019/20"
    const val VALIDITY = 1000 * 60 * 10 // 10 minutes in milliseconds
}

private fun addResponseToken(authentication: Authentication, response: HttpServletResponse, roles: List<RoleDAO>): String {

    val claims = HashMap<String, Any?>()
    claims["username"] = authentication.name
    claims["roles"] = roles.joinToString(", ")
    val token = Jwts
        .builder()
        .setClaims(claims)
        .setSubject(JWTSecret.SUBJECT)
        .setIssuedAt(Date(System.currentTimeMillis()))
        .setExpiration(Date(System.currentTimeMillis() + JWTSecret.VALIDITY))
        .signWith(SignatureAlgorithm.HS256, JWTSecret.KEY)
        .compact()

    response.addHeader("Authorization", "Bearer $token")
    return token
}

class UserPasswordAuthenticationFilterToJWT (
    defaultFilterProcessesUrl: String?,
    private val anAuthenticationManager: AuthenticationManager,
    private val users: UserService
) : AbstractAuthenticationProcessingFilter(defaultFilterProcessesUrl) {

    override fun attemptAuthentication(request: HttpServletRequest?,
                                       response: HttpServletResponse?): Authentication? {
        //getting user from request body
        val user = ObjectMapper().readValue(request!!.inputStream, UserDAO::class.java)

        // perform the "normal" authentication
        val auth = anAuthenticationManager.authenticate(UsernamePasswordAuthenticationToken(user.username, user.password))

        return if (auth.isAuthenticated) {
            // Proceed with an authenticated user
            SecurityContextHolder.getContext().authentication = auth
            auth
        } else
            null
    }

    override fun successfulAuthentication(request: HttpServletRequest,
                                          response: HttpServletResponse,
                                          filterChain: FilterChain?,
                                          auth: Authentication) {

        users.addToken(
            auth.name,
            addResponseToken(
                auth,
                response,
                users.findUser(auth.name)
                    .get().roles
            )
        )
            .ifPresentOrElse({},
                { response.sendError(HttpServletResponse.SC_NOT_FOUND) }
            )
    }
}

class UserAuthToken(private var login:String,
                    private val authorities: List<GrantedAuthority>) : Authentication {

    override fun getAuthorities() = authorities

    override fun setAuthenticated(isAuthenticated: Boolean) {}

    override fun getName() = login

    override fun getCredentials() = null

    override fun getPrincipal() = this

    override fun isAuthenticated() = true

    override fun getDetails() = login
}

class JWTAuthenticationFilter(
    private val users: UserService) : GenericFilterBean() {

    // To try it out, go to https://jwt.io to generate custom tokens, in this case we only need a name...

    override fun doFilter(request: ServletRequest?,
                          response: ServletResponse?,
                          chain: FilterChain?) {

        val authHeader = (request as HttpServletRequest).getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7) // Skip 7 characters for "Bearer "
            val claims = Jwts.parser()
                .setSigningKey(JWTSecret.KEY)
                .parseClaimsJws(token).body

            // should check for token validity here (e.g. expiration date, session in db, etc.)
            val exp = (claims["exp"] as Int).toLong()
            if ( exp < System.currentTimeMillis()/1000) // in seconds

                (response as HttpServletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED) // RFC 6750 3.1

            else {

                val authentication = UserAuthToken(
                    claims["username"] as String,
                    listOf(SimpleGrantedAuthority("ROLE_" + claims["role"]))
                )
                // Can go to the database to get the actual user information (e.g. authorities)

                SecurityContextHolder.getContext().authentication = authentication

                // Renew token with extended time here. (before doFilter)
                //check indexof (IndexOf func replacement)
                users.refreshToken(
                    authentication.name,
                    addResponseToken(
                        authentication,
                        response as HttpServletResponse,
                        users.findUser(authentication.name)
                            .get().roles
                    )
                )

                chain!!.doFilter(request, response)
            }
        } else {
            chain!!.doFilter(request, response)
        }
    }
}

/**
 * Instructions:
 *
 * http POST :8080/login username=user password=password
 *
 * Observe in the response:
 *
 * HTTP/1.1 200
 * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJKU09OIFdlYiBUb2tlbiBmb3IgQ0lBSSAyMDE5LzIwIiwiZXhwIjoxNTcxNzc2MTM4LCJpYXQiOjE1NzE3NDAxMzgsInVzZXJuYW1lIjoidXNlciJ9.Mz18cn5xw-7rBXw8KwlWxUDSsfNCqlliiwoIpvYPDzk
 *
 * http :8080/pets Authorization:"Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJKU09OIFdlYiBUb2tlbiBmb3IgQ0lBSSAyMDE5LzIwIiwiZXhwIjoxNTcxNzc2MTM4LCJpYXQiOjE1NzE3NDAxMzgsInVzZXJuYW1lIjoidXNlciJ9.Mz18cn5xw-7rBXw8KwlWxUDSsfNCqlliiwoIpvYPDzk"
 *
 */

class UserPasswordSignUpFilterToJWT (
    defaultFilterProcessesUrl: String?,
    private val users: UserService
) : AbstractAuthenticationProcessingFilter(defaultFilterProcessesUrl) {

    override fun attemptAuthentication(request: HttpServletRequest?,
                                       response: HttpServletResponse?): Authentication? {
        //getting user from request body
        val user = ObjectMapper().readValue(
            request!!.inputStream,
            UserDAO::class.java
        )
        val userDB = users.addUser(user)
        return if (userDB.isPresent)
            userDB.get()
                .let {
                    val auth = UserAuthToken(
                        user.username,
                        listOf(SimpleGrantedAuthority("ROLE_USER"))
                    )
                    SecurityContextHolder.getContext().authentication = auth
                    auth
                }
        else {
            (response as HttpServletResponse).sendError(HttpServletResponse.SC_CONFLICT)
            null
        }
    }


    override fun successfulAuthentication(request: HttpServletRequest,
                                          response: HttpServletResponse,
                                          filterChain: FilterChain?,
                                          auth: Authentication) {

    }

    class UserPasswordSignOutFilterToJWT(
        defaultFilterProcessesUrl: String?,
        private val users: UserService
    ) : AbstractAuthenticationProcessingFilter(defaultFilterProcessesUrl) {
        override fun attemptAuthentication(request: HttpServletRequest?,
                                           response: HttpServletResponse?): Authentication? {
            val authHeader = (request as HttpServletRequest).getHeader("Authorization")
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7) // Skip 7 characters for "Bearer "
                val claims = Jwts.parser()
                    .setSigningKey(JWTSecret.KEY)
                    .parseClaimsJws(token).body
                SecurityContextHolder.clearContext()
                val username = claims["username"] as String
                users.deleteUserTokens(username)
            } else {
                (response as HttpServletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED)
            }
            return null
        }
    }

}
