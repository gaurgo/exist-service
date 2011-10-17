package edu.mayo.cts2.framework.plugin.service.exist.profile.entitydescription;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.plugin.service.exist.dao.EntityDescriptionExistDao;
import edu.mayo.cts2.framework.plugin.service.exist.dao.ExistDao;
import edu.mayo.cts2.framework.plugin.service.exist.profile.AbstractExistReadService;
import edu.mayo.cts2.framework.plugin.service.exist.util.ExistServiceUtils;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionReadService;
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionName;

@Component
public class ExistEntityDescriptionReadService 
	extends AbstractExistReadService<
		EntityDescription,
		EntityDescriptionName,
		edu.mayo.cts2.framework.model.service.entitydescription.EntityDescriptionReadService>   
	implements EntityDescriptionReadService {

	@Resource
	private EntityDescriptionExistDao entityDescriptionExistDao;

	@Override
	public EntityDescription read(EntityDescriptionName identifier) {
		
		return 
				this.entityDescriptionExistDao.getResource(
						this.createPath(
								identifier.getCodeSystemVersionName()),
						ExistServiceUtils.getExistEntityName(identifier.getResourceId()));
	}

	@Override
	public boolean exists(EntityDescriptionName identifier) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ExistDao<?, EntityDescription> getExistDao() {
		return this.entityDescriptionExistDao;
	}
}
