package com.avesta.mastercrawler.utility;

import com.avesta.mastercrawler.model.News;
import com.avesta.mastercrawler.model.Tags;
import com.avesta.mastercrawler.model.Users;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Decides whose data the logged-in user may see.
 *
 * Users of type "Ai" only see the data they own (their news, their tags, their
 * report); Admin keeps seeing everything. The "User" type is left unchanged.
 *
 * Only call this from request threads — it reads the SecurityContext.
 */
public final class DataScope {

    public static final String OWN_DATA_ONLY_TYPE = "Ai";

    private DataScope() {
    }

    /** True when the logged-in user must be limited to their own data. */
    public static boolean isOwnDataOnly() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> OWN_DATA_ONLY_TYPE.equals(authority.getAuthority()));
    }

    /** The logged-in user's email (their username), or null when anonymous. */
    public static String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }

    /** Email to filter queries by, or null when the user may see everything. */
    public static String ownerEmailFilter() {
        return isOwnDataOnly() ? currentEmail() : null;
    }

    public static boolean canAccess(News news) {
        return news != null && (!isOwnDataOnly() || isCurrentUser(news.getUserId()));
    }

    public static boolean canAccess(Tags tag) {
        return tag != null && (!isOwnDataOnly() || isCurrentUser(tag.getUserId()));
    }

    private static boolean isCurrentUser(Users owner) {
        String email = currentEmail();
        return owner != null && owner.getEmail() != null && email != null
                && owner.getEmail().equalsIgnoreCase(email);
    }
}
