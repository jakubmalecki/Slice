package com.cognifide.slice.util;

/*
 * #%L
 * Slice - Core API
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 Cognifide Limited
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

/**
 * Util for getting injector name required for given request.
 * 
 * @author rafal.malinowski
 */
public final class InjectorNameUtil {

	private InjectorNameUtil() {
	}

	/**
	 * Gets injector name for given request. First a CRX tree is searched from current resource to top one.
	 * First found injectorName is returned. For each resource subresource jcr:content is also checked.
	 * 
	 * If no injectorName property is found then name is fetched from current resource type. It is name of
	 * path item directly after apps. For /apps/slice/... it will return slice.
	 */
	public static String getFromRequest(final SlingHttpServletRequest request) {
		final ResourceResolver resourceResolver = request.getResourceResolver();

		Resource currentResource = request.getResource();

		while (null != currentResource) {
			final String result = getFromResource(currentResource);
			if (StringUtils.isNotBlank(result)) {
				return result;
			}

			final String jcrContentResult = getFromResource(resourceResolver.getResource(currentResource,
					"jcr:content"));
			if (StringUtils.isNotBlank(jcrContentResult)) {
				return jcrContentResult;
			}

			currentResource = currentResource.getParent();
		}

		if (null != request.getResource()) {
			return getFromResourceType(request.getResource().getResourceType());
		}

		return StringUtils.EMPTY;
	}

	private static String getFromResource(final Resource currentResource) {
		if (null == currentResource) {
			return StringUtils.EMPTY;
		}

		final ValueMap resourceValueMap = currentResource.adaptTo(ValueMap.class);
		if (null != resourceValueMap) {
			final String injectorName = resourceValueMap.get("injectorName", String.class);
			if (null != injectorName) {
				return injectorName;
			}
		}

		return StringUtils.EMPTY;
	}

	private static String getFromResourceType(final String resourceType) {
		// we ignore absolute resource types at this stage
		// TODO: review and fix for absolute resource types (/apps/cognifide/...)
		return resourceType.substring(0, resourceType.indexOf('/'));
	}

}
