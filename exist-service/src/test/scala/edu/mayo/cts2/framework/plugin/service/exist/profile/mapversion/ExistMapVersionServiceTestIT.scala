package edu.mayo.cts2.framework.plugin.service.exist.profile.mapversion

import org.springframework.beans.factory.annotation.Autowired
import edu.mayo.cts2.framework.model.service.exception.UnknownMapVersion
import edu.mayo.cts2.framework.model.service.exception.UnknownResourceReference
import edu.mayo.cts2.framework.model.mapversion.MapVersion
import edu.mayo.cts2.framework.model.core.SourceAndNotation
import edu.mayo.cts2.framework.model.core.MapReference
import org.junit.Test
import edu.mayo.cts2.framework.model.mapversion.MapVersionDirectoryEntry
import edu.mayo.cts2.framework.plugin.service.exist.profile.BaseServiceTestBaseIT
import edu.mayo.cts2.framework.service.name.Name

class ExistMapVersionServiceTestIT extends BaseServiceTestBaseIT[MapVersion,MapVersionDirectoryEntry] {
  
    @Autowired var readService:ExistMapVersionReadService = null
    @Autowired var maintService:ExistMapVersionMaintenanceService = null
 
    def getExceptionClass():Class[_<:UnknownResourceReference] = {
       classOf[UnknownMapVersion]
    }
  
    def createResource(name:String, uri:String) = {
      var entry = new MapVersion()
      entry.setMapVersionName(name);
      entry.setAbout("about")
      entry.setDocumentURI(uri)
      entry.setSourceAndNotation(new SourceAndNotation());
      entry.setVersionOf(new MapReference())
      entry.getVersionOf().setContent("map")

      maintService.createResource("", entry)
    }
      
    def getResource(name:String):MapVersion = {
    	readService.read(new Name(name))
    }
     
    def getResourceByUri(uri:String):MapVersion = {
    	readService.readByUri(uri)
    }
}