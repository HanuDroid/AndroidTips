/**
 * 
 */
package org.varunverma.androidtips;

import android.content.SearchRecentSuggestionsProvider;

/**
 * @author Varun
 *
 */
public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {

	final static String AUTHORITY = "org.varunverma.androidtips.SearchSuggestionProvider";
    final static int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}