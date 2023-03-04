package io.github.alessandrojean.tankobon.infrastructure.security

import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class TankobonPrincipal(val user: TankobonUser) : UserDetails {

  override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
    val roles = listOfNotNull(
      SimpleGrantedAuthority("ROLE_ADMIN").takeIf { user.isAdmin },
      SimpleGrantedAuthority("ROLE_USER")
    )
    
    return roles.toMutableSet()
  }

  override fun isEnabled() = true

  override fun getUsername() = user.email

  override fun isCredentialsNonExpired() = true

  override fun getPassword() = user.password

  override fun isAccountNonExpired() = true

  override fun isAccountNonLocked() = true
}
