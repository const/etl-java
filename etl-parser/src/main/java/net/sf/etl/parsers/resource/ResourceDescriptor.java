/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2012 Constantine A Plotnikov
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.sf.etl.parsers.resource;

import java.io.Serializable;
import java.util.*;

/**
 * The resource descriptor.
 */
public class ResourceDescriptor implements Serializable {
    /**
     * The systemId (systemId)
     */
    private final String systemId;
    /**
     * Resolved resource type
     */
    private final String type;
    /**
     * The resolved version
     */
    private final String version;
    /**
     * Resource usages
     */
    private final List<ResourceUsage> usedResources;

    public ResourceDescriptor(String systemId, String type, String version, Collection<ResourceUsage> usedResources) {
        this.systemId = systemId;
        this.type = type;
        this.version = version;
        this.usedResources = usedResources == null || usedResources.isEmpty() ?
                Collections.<ResourceUsage>emptyList() :
                Collections.unmodifiableList(Arrays.asList(usedResources.toArray(new ResourceUsage[usedResources.size()])));
    }

    public ResourceDescriptor(String systemId, String type, String version) {
        this(systemId, type, version, null);
    }

    public ResourceDescriptor(String systemId) {
        this(systemId, null, null);
    }

    public ResourceDescriptor withType(String type) {
        return new ResourceDescriptor(systemId, type, version, usedResources);
    }

    public ResourceDescriptor withVersion(String version) {
        return new ResourceDescriptor(systemId, type, version, usedResources);
    }

    /**
     * Add more resource usages for the map
     *
     * @param additionalResources additional resources to add
     * @return the resources
     */
    public ResourceDescriptor withAdditionalResources(Collection<ResourceUsage> additionalResources) {
        if (additionalResources == null || additionalResources.isEmpty()) {
            return this;
        }
        LinkedHashMap<ResourceUsage, String> newUsages = new LinkedHashMap<ResourceUsage, String>();
        for (ResourceUsage resourceUsage : usedResources) {
            newUsages.put(resourceUsage, "");
        }
        for (ResourceUsage resourceUsage : additionalResources) {
            newUsages.put(resourceUsage, "");
        }
        if (newUsages.size() == usedResources.size()) {
            // nothing new was added
            return this;
        }
        return new ResourceDescriptor(systemId, type, version, newUsages.keySet());
    }


    public String getSystemId() {
        return systemId;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public List<ResourceUsage> getUsedResources() {
        return usedResources;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ResourceDescriptor");
        sb.append("{systemId='").append(systemId).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", usedResources=").append(usedResources);
        sb.append('}');
        return sb.toString();
    }
}
