package edu.mayo.cts2.framework.plugin.service.exist.profile.valueset;

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.filter.match.StateAdjustingPropertyReference;
import edu.mayo.cts2.framework.filter.match.StateAdjustingPropertyReference.StateUpdater;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntrySummary;
import edu.mayo.cts2.framework.plugin.service.exist.profile.AbstractExistQueryService;
import edu.mayo.cts2.framework.plugin.service.exist.profile.PathInfo;
import edu.mayo.cts2.framework.plugin.service.exist.restrict.directory.XpathDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.exist.restrict.directory.XpathDirectoryBuilder.XpathState;
import edu.mayo.cts2.framework.plugin.service.exist.xpath.XpathStateUpdater;
import edu.mayo.cts2.framework.service.command.restriction.ValueSetQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQuery;
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQueryService;

@Component
public class ExistValueSetQueryService 
	extends AbstractExistQueryService
		<ValueSetCatalogEntry,
		ValueSetCatalogEntrySummary,
		ValueSetQueryServiceRestrictions,
		XpathState>
	implements ValueSetQueryService {

	@Resource
	private ValueSetResourceInfo valueSetResourceInfo;
	
	@Override
	protected ValueSetCatalogEntrySummary createSummary() {
		return new ValueSetCatalogEntrySummary();
	}

	public Set<StateAdjustingPropertyReference<XpathState>> getSupportedSearchReferences() {
		Set<StateAdjustingPropertyReference<XpathState>> set = super.getSupportedSearchReferences();
		
		StateAdjustingPropertyReference<XpathState> resourceSynopsis = 
				StateAdjustingPropertyReference.toPropertyReference(
						StandardModelAttributeReference.RESOURCE_SYNOPSIS.getPropertyReference(),
						getResourceSynopsisStateUpdater());
			
		set.add(resourceSynopsis);
		
		return set;
	}
	
	private StateUpdater<XpathState> getResourceSynopsisStateUpdater() {
		return new XpathStateUpdater<XpathState>(".//core:resourceSynopsis/core:value/text()");
	}
	
	@Override
	protected ValueSetCatalogEntrySummary doTransform(
			ValueSetCatalogEntry resource,
			ValueSetCatalogEntrySummary summary, org.xmldb.api.base.Resource eXistResource11) {
		summary = this.baseTransform(summary, resource);
		
		summary.setValueSetName(resource.getValueSetName());
		summary.setHref(getUrlConstructor().createValueSetUrl(resource.getValueSetName()));
		
		return summary;
	}

	private class ValueSetDirectoryBuilder extends XpathDirectoryBuilder<XpathState,ValueSetCatalogEntrySummary> {

		public ValueSetDirectoryBuilder(final String changeSetUri) {
			super(new XpathState(), new Callback<XpathState, ValueSetCatalogEntrySummary>() {

				@Override
				public DirectoryResult<ValueSetCatalogEntrySummary> execute(
						XpathState state, 
						int start, 
						int maxResults) {
					return getResourceSummaries(
							getResourceInfo(),
							changeSetUri,
							"",
							state.getXpath(), 
							start, 
							maxResults);
				}

				@Override
				public int executeCount(XpathState state) {
					throw new UnsupportedOperationException();
				}},
				
				getSupportedMatchAlgorithms(),
				getSupportedSearchReferences());
		}
	}

	@Override
	public DirectoryResult<ValueSetCatalogEntrySummary> getResourceSummaries(
			ValueSetQuery query, 
			SortCriteria sort,
			Page page) {

	ValueSetDirectoryBuilder builder =
			new ValueSetDirectoryBuilder(
					this.getChangeSetUri(
							query.getReadContext()));
	
	return 
		builder.restrict(query.getFilterComponent()).
			addStart(page.getStart()).
			addMaxToReturn(page.getMaxToReturn()).
			resolve();
	}

	@Override
	public DirectoryResult<ValueSetCatalogEntry> getResourceList(
			ValueSetQuery query, 
			SortCriteria sort,
			Page page) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int count(
			ValueSetQuery query) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected PathInfo getResourceInfo() {
		return this.valueSetResourceInfo;
	}

}
