package com.berrycloud.acl.search;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;

/**
 * <p>
 * A special {@link Sort} option for queries.
 * </p>
 * <p>
 * The result will be sorted by the relevance of the search result. It also automatically applies a search predicate to
 * the query.
 * </p>
 * The only reason this class extends the original {@link Sort} class is that Data JPA handles the 'special' arguments
 * in a way that the argument types cannot be be extended. So technically the controller and repository methods will
 * accept a {@link Sort} argument, but when that argument is processed then a completely different logic will take place
 * if the actual instance type of that argument is the Search subclass.
 * 
 * @author István Rátkai (Selindek)
 *
 */
public class Search extends Sort {

  private static final long serialVersionUID = -2539103205396858403L;

  private static final Search NONE = new Search();

  private List<String> patterns;

  @SuppressWarnings("deprecation")
  private Search() {
    // We have to use the deprecated constructor, because the new ones checks for the empty array.
    super(new Order[0]);
  }

  @SuppressWarnings("deprecation")
  public Search(String patterns) {
    super(new Order[0]);
    this.patterns = Arrays.stream(patterns.split("[\\s]+")).map(String::toLowerCase).filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  @SuppressWarnings("deprecation")
  public Search(List<String> patterns) {
    super(new Order[0]);
    this.patterns = patterns.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
  }

  public List<String> getPatterns() {
    return patterns;
  }

  @Override
  public boolean isSorted() {
    return true;
  }

  /**
   * Returns a {@link Search} instances representing no search setup at all.
   * 
   * @return
   */
  public static Search none() {
    return NONE;
  }

}
