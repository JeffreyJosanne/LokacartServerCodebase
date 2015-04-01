package app.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import app.entities.Organization;
import app.entities.OrganizationMembership;
import app.entities.User;
import app.entities.UserPhoneNumber;
import app.rest.repositories.UserPhoneNumberRepository;

public class AuthenticatedUser implements UserDetails {
	private static final long serialVersionUID = 1L;

	int userId;
	String username;
	String password;
	List<GrantedAuthority> authorities;

	public AuthenticatedUser(User user, UserPhoneNumberRepository userPhoneNumberRepository) {
		userId = user.getUserId();
		password = user.getSha256Password();

		username = user.getEmail();
		if (username == null || username.isEmpty()) {
			UserPhoneNumber number = userPhoneNumberRepository.findByUserAndPrimary(user, 1);
			username = number.getPhoneNumber();
			if (username == null || username.isEmpty())
				username = "@User" + user.getUserId(); // we should probably never come to this
		}

		Set<String> authoritiesSet = new HashSet<String>();
		List<OrganizationMembership> memberships = user.getOrganizationMemberships();
		for (OrganizationMembership membership : memberships) {
			Organization organization = membership.getOrganization();
			authoritiesSet.add("MEMBER" + organization.getOrganizationId());
			if (membership.getIsPublisher()) {
				authoritiesSet.add("PUBLISHER" + organization.getOrganizationId());
				authoritiesSet.add("ADMIN_OR_PUBLISHER" + organization.getOrganizationId());
			}
			if (membership.getIsAdmin()) {
				authoritiesSet.add("ADMIN" + organization.getOrganizationId());
				authoritiesSet.add("ADMIN_OR_PUBLISHER" + organization.getOrganizationId());
			}
		}
		authorities = AuthorityUtils.createAuthorityList(authoritiesSet.toArray(new String[authoritiesSet.size()]));
	}

	public int getUserId() {
		return userId;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
