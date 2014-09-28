/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2013 Constantine A Plotnikov
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The resource descriptor.
 */
public final class ResourceDescriptor implements Serializable {
    /**
     * UID.
     */
    private static final long serialVersionUID = -4760267073222818689L;
    /**
     * The systemId that uniquely identifies the resource in the system.
     */
    private final String systemId;
    /**
     * Resolved resource type.
     */
    private final String type;
    /**
     * The resolved version.
     */
    private final String version;
    /**
     * Resource usages.
     */
    private final List<ResourceUsage> usedResources;

    /**
     * The base constructor.
     *
     * @param systemId      the base system id
     * @param type          the resource type
     * @param version       the resource version
     * @param usedResources the resource constructor
     */
    public ResourceDescriptor(final String systemId, final String type, final String version,
                              final Collection<ResourceUsage> usedResources) {
        if (systemId == null) {
            throw new IllegalArgumentException("The null is not allowed for the systemId");
        }
        this.systemId = systemId;
        this.type = type;
        this.version = version;
        final LinkedHashMap<ResourceUsage, String> newUsages = new LinkedHashMap<ResourceUsage, String>();
        if (usedResources != null) {
            for (final ResourceUsage resourceUsage : usedResources) {
                final ResourceUsage filtered = filter(systemId, resourceUsage);
                if (filtered != null) {
                    newUsages.put(filtered, "");
                }
            }
        }
        this.usedResources = newUsages.isEmpty()
                ? Collections.<ResourceUsage>emptyList()
                : Collections.unmodifiableList(Arrays.asList(newUsages.keySet().toArray(
                new ResourceUsage[newUsages.size()])));
    }

    /**
     * The utility constructor.
     *
     * @param systemId the base system id
     * @param type     the resource type
     * @param version  the resource version
     */
    public ResourceDescriptor(final String systemId, final String type, final String version) {
        this(systemId, type, version, null);
    }

    /**
     * The utility constructor.
     *
     * @param systemId the base system id
     */
    public ResourceDescriptor(final String systemId) {
        this(systemId, null, null);
    }

    /**
     * Create copy of the descriptor with the specified type.
     *
     * @param newType the new resource type
     * @return the new instance of resource descriptor
     */
    public ResourceDescriptor withType(final String newType) {
        return new ResourceDescriptor(systemId, newType, version, usedResources);
    }

    /**
     * Create copy of the descriptor with the specified version.
     *
     * @param newVersion the new resource version
     * @return the new instance of resource descriptor
     */
    public ResourceDescriptor withVersion(final String newVersion) {
        return new ResourceDescriptor(systemId, type, newVersion, usedResources);
    }

    /**
     * Add more resource usages for the descriptor.
     *
     * @param additionalResources additional resources to add
     * @return the resource descriptor with more resources
     */
    public ResourceDescriptor withAdditionalResources(final Collection<ResourceUsage> additionalResources) {
        if (additionalResources == null || additionalResources.isEmpty()) {
            return this;
        }
        final LinkedHashMap<ResourceUsage, String> newUsages = new LinkedHashMap<ResourceUsage, String>();
        for (final ResourceUsage resourceUsage : usedResources) {
            newUsages.put(resourceUsage, "");
        }
        for (final ResourceUsage resourceUsage : additionalResources) {
            newUsages.put(resourceUsage, "");
        }
        if (newUsages.size() == usedResources.size()) {
            // nothing new was added
            return this;
        }
        return new ResourceDescriptor(systemId, type, version, newUsages.keySet());
    }

    /**
     * Filter resource usage so it does not contain additional references from resource with
     * the same system id as a root system id.
     *
     * @param filteredSystemId the system id to filter out
     * @param resourceUsage    the resource usage
     * @return the filtered out resource usage
     */
    private ResourceUsage filter(final String filteredSystemId, final ResourceUsage resourceUsage) {
        final ResourceDescriptor descriptor = resourceUsage.getDescriptor();
        if (descriptor.getSystemId().equals(filteredSystemId)) {
            // the descriptor without additional usages
            return new ResourceUsage(resourceUsage.getReference(),
                    new ResourceDescriptor(descriptor.getSystemId(),
                            descriptor.getType(),
                            descriptor.getVersion()), resourceUsage.getRole());
        }
        boolean changed = false;
        int i = 0;
        ResourceUsage changedResource = null;
        final List<ResourceUsage> resources = descriptor.getUsedResources();
        for (final ResourceUsage used : resources) {
            changedResource = filter(filteredSystemId, used);
            if (changedResource != used) { // NOPMD
                changed = true;
                break;
            }
            i++;
        }
        if (changed) {
            final ArrayList<ResourceUsage> newUsages = new ArrayList<ResourceUsage>();
            if (i > 0) {
                newUsages.addAll(resources.subList(0, i));
            }
            if (changedResource != null) {
                newUsages.add(changedResource);
            }
            for (final ResourceUsage used : resources.subList(i + 1, resources.size())) {
                changedResource = filter(filteredSystemId, used);
                if (changedResource != null) {
                    newUsages.add(changedResource);
                }
            }
            return new ResourceUsage(resourceUsage.getReference(),
                    new ResourceDescriptor(descriptor.getSystemId(),
                            descriptor.getType(),
                            descriptor.getVersion(),
                            newUsages), resourceUsage.getRole());
        } else {
            return resourceUsage;
        }
    }

    /**
     * @return the resolved system id that uniquely identifies resource in the system
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @return the resource type, null if unknown
     */
    public String getType() {
        return type;
    }

    /**
     * @return the resource version, none if unknown
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return used resources, empty list if unknown
     */
    public List<ResourceUsage> getUsedResources() {
        return usedResources;
    }


    @Override
    public String toString() {
        return "ResourceDescriptor{systemId='" + systemId + "', type='" + type + "', version='" + version
                + "', usedResources=" + usedResources + '}';
    }

    @Override
    public boolean equals(final Object o) {
        //CHECKSTYLE:OFF
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ResourceDescriptor that = (ResourceDescriptor) o;

        if (!systemId.equals(that.systemId)) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (usedResources != null ? !usedResources.equals(that.usedResources) : that.usedResources != null) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
        //CHECKSTYLE:ON
    }

    @Override
    public int hashCode() {
        //CHECKSTYLE:OFF
        int result = systemId.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (usedResources != null ? usedResources.hashCode() : 0);
        return result;
        //CHECKSTYLE:ON
    }
}
