package edu.mayo.cts2.framework.plugin.service.exist.dao;

import org.apache.commons.lang.StringUtils;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.CompiledExpression;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XQueryService;

import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.exception.UnspecifiedCts2RuntimeException;

public abstract class AbstractResourceExistDao<S,R> extends AbstractExistDao implements ExistDao<S,R> {

	public DirectoryResult<S> getResourceSummaries(String path, String xpath,
			int start, int max) {
		String collectionPath = this.getResourcePath(path);

		return this.getResourceSummaries(collectionPath, xpath, start, max,
				transform);
	}

	public void storeResource(String path, R entry) {
		Collection collection = null;
		try {
			collection = this.getExistManager()
					.getOrCreateCollection(this.getResourcePath(path));

			this.createAndStoreResource(entry, collection, getName(entry) );

		} catch (XMLDBException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if(collection != null){
					collection.close();
				}
			} catch (XMLDBException e) {
				this.log.warn(e);
			}
		}
	}

	protected abstract String getName(R entry);

	private String getResourcePath(String path) {
		String basePath = StringUtils.removeStart(this.doGetResourceBasePath(),
				"/");
		path = StringUtils.removeStart(path, "/");
		return CTS2_RESOURCES_PATH + "/" + basePath + "/" + path;
	}

	protected abstract String doGetResourceBasePath();

	@SuppressWarnings("unchecked")
	public R getResource(String path, String name) {
		Resource resource = this.doGetResource(name, this.getResourcePath(path));

		if (resource == null) {
			return null;
		}

		return (R) this.unmarshallResource(resource);

	}
	
	@SuppressWarnings("unchecked")
	protected R getResourceByXpath(String collectionPath, String xpathQuery) {
		Resource resource;
		try {
			XQueryService xqueryService = this.getExistManager().getXQueryService(collectionPath);
			
			CompiledExpression expression = 
					xqueryService.compile(xpathQuery);
			
			ResourceSet resourceSet = xqueryService.execute(expression);

			long size = resourceSet.getSize();
			
			if (size == 0) {
				return null;
			}
			
			//this should be caught during insert. If we get this far, the service is in an
			//illegal state.
			if (size > 1) {
				throw new IllegalStateException("Duplicate Entries found.");
			}

			resource = resourceSet.getResource(0);
		} catch (XMLDBException e) {
			throw new UnspecifiedCts2RuntimeException(e);
		}
		
		return (R) this.unmarshallResource(resource);

	}
	
	protected abstract String getResourceXpath();
	
	protected abstract String getUriXpath();

	public R getResourceByUri(String collectionPath, String uri) {
		String expressionString = 
				this.getResourceXpath() + "[" + this.getUriXpath() + "='" + uri + "']";
	
		return this.getResourceByXpath(collectionPath, expressionString);
	}

	protected abstract S createSummary();

	protected abstract S doTransform(R resource, S summary,
			Resource eXistResource);

	private SummaryTransform<S, R> transform = new SummaryTransform<S, R>() {

		@Override
		public S createSummary() {
			return AbstractResourceExistDao.this.createSummary();
		}

		@Override
		public S transform(R resource, Resource eXistResource) {
			return AbstractResourceExistDao.this.doTransform(resource,
					this.createSummary(), eXistResource);
		}
	};

	protected String doGetNameFromResource(Resource resource, int index) {
		String collectionPath;
		try {
			collectionPath = resource.getParentCollection().getName();
		} catch (XMLDBException e) {
			throw new IllegalStateException();
		}
		String path = StringUtils.substringAfter(collectionPath,
				this.doGetResourceBasePath());

		path = StringUtils.removeStart(path, "/");
		path = StringUtils.removeEnd(path, "/");

		String[] pathTokens = path.split("/");

		return pathTokens[index];
	}
}
