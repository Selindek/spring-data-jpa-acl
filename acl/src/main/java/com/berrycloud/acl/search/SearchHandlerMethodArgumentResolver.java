package com.berrycloud.acl.search;

import static org.springframework.hateoas.TemplateVariable.VariableType.REQUEST_PARAM;
import static org.springframework.hateoas.TemplateVariable.VariableType.REQUEST_PARAM_CONTINUED;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariable.VariableType;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class SearchHandlerMethodArgumentResolver extends HateoasSortHandlerMethodArgumentResolver {

  private static final String DEFAULT_PARAMETER = "search";
  private static final String DEFAULT_PROPERTY_DELIMITER = " ";
  private static final String DEFAULT_QUALIFIER_DELIMITER = "_";

  private String searchParameter = DEFAULT_PARAMETER;
  private String propertyDelimiter = DEFAULT_PROPERTY_DELIMITER;
  private String qualifierDelimiter = DEFAULT_QUALIFIER_DELIMITER;

  /**
   * Configure the request parameter to lookup search information from. Defaults to {@code search}.
   * 
   * @param searchParameter
   *          must not be {@literal null} or empty.
   */
  public void setSearchParameter(String searchParameter) {

    Assert.hasText(searchParameter, "SearchParameter must not be null nor empty!");
    this.searchParameter = searchParameter;
  }

  /**
   * Configures the delimiter used to separate search patterns. Defaults to {@code ' '}, which means search values look
   * like this: {@code foo bar}.
   * 
   * @param propertyDelimiter
   *          must not be {@literal null} or empty.
   */
  @Override
  public void setPropertyDelimiter(String propertyDelimiter) {
    super.setPropertyDelimiter(propertyDelimiter);
    this.propertyDelimiter = propertyDelimiter;
  }

  /**
   * Configures the delimiter used to separate the qualifier from the search parameter. Defaults to {@code _}, so a
   * qualified search property would look like {@code qualifier_search}.
   * 
   * @param qualifierDelimiter
   *          the qualifier delimiter to be used or {@literal null} to reset to the default.
   */
  @Override
  public void setQualifierDelimiter(String qualifierDelimiter) {
    super.setQualifierDelimiter(qualifierDelimiter);
    this.qualifierDelimiter = qualifierDelimiter == null ? DEFAULT_QUALIFIER_DELIMITER : qualifierDelimiter;
  }

  /**
   * <p>
   * Tries to resolve a search parameter from the request.
   * </p>
   * If there is no search parameter then falls back resolving a sort parameter.
   */
  @Override
  public Sort resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

    String[] searchParameter = webRequest.getParameterValues(getSearchParameter(parameter));

    // No parameter
    if (searchParameter == null) {
      return super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
    }

    List<String> patterns = Arrays.stream(searchParameter).map(String::toLowerCase).map(s -> s.split(propertyDelimiter))
        .flatMap(a -> Arrays.stream(a)).filter(s -> !s.isEmpty()).collect(Collectors.toList());

    return new Search(patterns);
  }

  /**
   * Returns the search parameter to be looked up from the request.
   * 
   * @param parameter
   *          can be {@literal null}.
   * @return
   */
  protected String getSearchParameter(MethodParameter parameter) {

    StringBuilder builder = new StringBuilder();

    Qualifier qualifier = parameter != null ? parameter.getParameterAnnotation(Qualifier.class) : null;

    if (qualifier != null) {
      builder.append(qualifier.value()).append(qualifierDelimiter);
    }

    return builder.append(searchParameter).toString();
  }

  /**
   * Returns the template variables for the search AND sort parameter.
   * 
   * @param parameter
   *          must not be {@literal null}.
   * @return
   * @since 1.7
   */
  @Override
  public TemplateVariables getSortTemplateVariables(MethodParameter parameter, UriComponents template) {

    TemplateVariables variables = super.getSortTemplateVariables(parameter, template);

    String searchParameter = getSearchParameter(parameter);
    MultiValueMap<String, String> queryParameters = template.getQueryParams();
    boolean append = !queryParameters.isEmpty();

    if (queryParameters.containsKey(searchParameter)) {
      return variables;
    }

    String description = String.format("pagination.%s.description", searchParameter);
    VariableType type = append ? REQUEST_PARAM_CONTINUED : REQUEST_PARAM;
    return variables.concat(new TemplateVariable(searchParameter, type, description));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.springframework.hateoas.mvc.UriComponentsContributor#enhance(org.springframework.web.util.UriComponentsBuilder,
   * org.springframework.core.MethodParameter, java.lang.Object)
   */
  @Override
  public void enhance(UriComponentsBuilder builder, MethodParameter parameter, Object value) {

    if (!(value instanceof Search)) {
      super.enhance(builder, parameter, value);
      return;
    }

    String searchParameter = getSearchParameter(parameter);

    builder.replaceQueryParam(searchParameter);
    builder.queryParam(searchParameter, ((Search) value).getPatterns());

  }

}
