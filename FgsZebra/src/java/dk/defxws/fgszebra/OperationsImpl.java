//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fgszebra;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import dk.defxws.fedoragsearch.server.GTransformer;
import dk.defxws.fedoragsearch.server.GenericOperationsImpl;
import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

/**
 * performs the Zebra specific parts of the operations
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class OperationsImpl extends GenericOperationsImpl {

	private static final Logger logger = Logger.getLogger(OperationsImpl.class);

	private IndexModifier modifier = null;
	private int[] counts = { 0, 0, 0};

	public String gfindObjects(
			String query,
			int hitPageStart,
			int hitPageSize,
			int snippetsMax,
			int fieldMaxLength,
			String indexName,
            String sortFields,
			String resultPageXslt)
	throws java.rmi.RemoteException {
		super.gfindObjects(query, hitPageStart, hitPageSize, snippetsMax, fieldMaxLength, indexName, sortFields, resultPageXslt);
		ResultSet resultSet = (new Connection()).createStatement().executeQuery(
				query,
				hitPageStart,
				hitPageSize,
				config.getIndexBase(indexName),
				config.getIndexName(indexName));
		if (logger.isDebugEnabled())
			logger.debug("resultSet.getResultXml()="+resultSet.getResultXml());
		params[12] = "RESULTPAGEXSLT";
		params[13] = resultPageXslt;
        String xsltPath = config.getConfigName()+"/index/"+config.getIndexName(indexName)+"/"+config.getGfindObjectsResultXslt(indexName, resultPageXslt);
        StringBuffer sb = (new GTransformer()).transform(
        		xsltPath,
				new StreamSource(resultSet.getResultXml()),
				params);
		return sb.toString();
	}

	public String browseIndex(
			String startTerm,
			int termPageSize,
			String fieldName,
			String indexName,
			String resultPageXslt)
	throws java.rmi.RemoteException {
		super.browseIndex(startTerm, termPageSize, fieldName, indexName, resultPageXslt);
		ResultSet resultSet = (new Connection()).createStatement().executeScan(
				startTerm,
				termPageSize,
				fieldName,
				config.getIndexBase(indexName),
				config.getIndexName(indexName));
		if (logger.isDebugEnabled())
			logger.debug("resultSet.getResultXml()="+resultSet.getResultXml());
		params[10] = "RESULTPAGEXSLT";
		params[11] = resultPageXslt;
        String xsltPath = config.getConfigName()+"/index/"+config.getIndexName(indexName)+"/"+config.getBrowseIndexResultXslt(indexName, resultPageXslt);
        StringBuffer sb = (new GTransformer()).transform(
        		xsltPath,
				new StreamSource(resultSet.getResultXml()),
				params);
		return sb.toString();
	}

	public String getIndexInfo(
			String indexName,
			String resultPageXslt)
	throws java.rmi.RemoteException {
		super.getIndexInfo(indexName, resultPageXslt);
		InputStream infoStream =  null;
		String indexInfoPath = "/config/index/"+config.getIndexName(indexName)+"/indexInfo.xml";
		try {
			infoStream =  OperationsImpl.class.getResourceAsStream(indexInfoPath);
			if (infoStream == null) {
				throw new GenericSearchException("Error "+indexInfoPath+" not found in classpath");
			}
		} catch (IOException e) {
			throw new GenericSearchException("Error "+indexInfoPath+" not found in classpath", e);
		}
        String xsltPath = config.getConfigName()+"/index/"+config.getIndexName(indexName)+"/"+config.getIndexInfoResultXslt(indexName, resultPageXslt);
        StringBuffer sb = (new GTransformer()).transform(
        		xsltPath,
				new StreamSource(infoStream),
				new String[] {});
		return sb.toString();
	}

	public String updateIndex(
			String action,
			String value,
			String repositoryName,
			String indexName,
			String indexDocXslt,
			String resultPageXslt)
	throws java.rmi.RemoteException {
		insertTotal = 0;
		deleteTotal = 0;
		StringBuffer resultXml = new StringBuffer(); 
		resultXml.append("<zebraUpdateIndex");
		resultXml.append(" indexName=\""+indexName+"\"");
		resultXml.append(">\n");
		try {
			if ("createEmpty".equals(action)) 
				createEmpty(config.getIndexDir(indexName), resultXml);
			else {
				modifier = new IndexModifier(config.getIndexDir(indexName), false);
				counts = modifier.getCounts();
				docCount = counts[0] - counts[2];
				if ("fromFoxmlFiles".equals(action)) 
					fromFoxmlFiles(value, repositoryName, indexName, resultXml, indexDocXslt);
				else
					if ("fromPid".equals(action)) 
						fromPid(value, repositoryName, indexName, resultXml, indexDocXslt);
					else
						if ("deletePid".equals(action)) 
							deletePid(value, indexName, resultXml);
			}
		} catch (RemoteException e) {
			if (modifier != null) {
				try {
					modifier.close();
				} catch (IOException ioe) {
				}
			}
			throw new GenericSearchException("Exception on updateIndex action= "+action, e);
		}
		counts[0] += insertTotal;
		counts[1] += updateTotal;
		counts[2] += deleteTotal;
		if (modifier != null) {
			try {
				modifier.setCounts(counts);
				modifier.close();
			} catch (IOException e) {
				throw new GenericSearchException("IndexModifier close error", e);
			}
		}
		resultXml.append("<counts");
		resultXml.append(" insertTotal=\""+insertTotal+"\"");
		resultXml.append(" updateTotal=\""+updateTotal+"\"");
		resultXml.append(" deleteTotal=\""+deleteTotal+"\"");
		resultXml.append(" docCount=\""+(counts[0]-counts[2])+"\"");
		resultXml.append("/>\n");
		resultXml.append("</zebraUpdateIndex>\n");
		params = new String[12];
		params[0] = "OPERATION";
		params[1] = "updateIndex";
		params[2] = "ACTION";
		params[3] = action;
		params[4] = "VALUE";
		params[5] = value;
		params[6] = "REPOSITORYNAME";
		params[7] = repositoryName;
		params[8] = "INDEXNAME";
		params[9] = indexName;
		params[10] = "RESULTPAGEXSLT";
		params[11] = resultPageXslt;
        String xsltPath = config.getConfigName()+"/index/"+config.getIndexName(indexName)+"/"+config.getUpdateIndexResultXslt(indexName, resultPageXslt);
        StringBuffer sb = (new GTransformer()).transform(
        		xsltPath,
				resultXml,
				params);
		return sb.toString();
	}

	private void createEmpty(
			String filePath,
			StringBuffer resultXml)
	throws java.rmi.RemoteException {
		modifier = new IndexModifier(filePath, true);
		modifier.runCreateEmpty(resultXml);
	}

	private void fromFoxmlFiles(
			String filePath,
			String repositoryName,
			String indexName,
			StringBuffer resultXml,
			String indexDocXslt)
	throws java.rmi.RemoteException {
		File objectDir = null;
		if (filePath==null || filePath.equals(""))
			objectDir = config.getFedoraObjectDir(repositoryName);
		else objectDir = new File(filePath);
		indexDocs(objectDir, repositoryName, indexName, resultXml, indexDocXslt);
	}

	private void indexDocs(
			File file, 
			String repositoryName,
			String indexName,
			StringBuffer resultXml, 
			String indexDocXslt)
	throws java.rmi.RemoteException
	{
		if (file.isHidden()) return;
		if (file.isDirectory())
		{
			String[] files = file.list();
			for (int i = 0; i < files.length; i++)
				indexDocs(new File(file, files[i]), repositoryName, indexName, resultXml, indexDocXslt);
		}
		else
		{
			try {
				indexDoc(file.getName(), repositoryName, indexName, new FileInputStream(file), resultXml, indexDocXslt);
			} catch (RemoteException e) {
				throw new GenericSearchException("Error file="+file.getAbsolutePath(), e);
			} catch (FileNotFoundException e) {
				throw new GenericSearchException("Error file="+file.getAbsolutePath(), e);
			}
		}
	}

	private void fromPid(
			String pid,
			String repositoryName,
			String indexName,
			StringBuffer resultXml,
			String indexDocXslt)
	throws java.rmi.RemoteException {
		getFoxmlFromPid(pid, repositoryName);
		indexDoc(pid, repositoryName, indexName, new ByteArrayInputStream(foxmlRecord), resultXml, indexDocXslt);
	}

	private void deletePid(
			String pid,
			String indexName,
			StringBuffer resultXml)
	throws java.rmi.RemoteException {
		int[] dcounts= modifier.runUpdate(null, "deletePid "+pid, config.getIndexDir(indexName));
		insertTotal += dcounts[0];
		updateTotal += dcounts[1];
		deleteTotal += dcounts[2];
		docCount = insertTotal - deleteTotal;
		resultXml.append("<delete>"+pid+"</delete>\n");
        logger.info("deletePid="+pid+" docCount="+docCount);
	}

	private void indexDoc(
			String pidOrFilename,
			String repositoryName,
			String indexName,
			InputStream foxmlStream,
			StringBuffer resultXml,
			String indexDocXslt)
	throws java.rmi.RemoteException {
		StringBuffer sb = null;
        String xsltPath = config.getConfigName()+"/index/"+indexName+"/"+config.getUpdateIndexDocXslt(indexName, indexDocXslt);
		(new GTransformer()).transformToFile(
        		xsltPath,
				new StreamSource(foxmlStream),
				new Object[] {"REPOSITORYNAME", repositoryName,
					"OPERATIONSIMPL", this},
				config.getIndexDir(indexName)+"/temp_records/sb");
		int[] icounts= modifier.runUpdate(sb, "update", config.getIndexDir(indexName));
		insertTotal += icounts[0];
		updateTotal += icounts[1];
		deleteTotal += icounts[2];
		docCount = insertTotal - deleteTotal;
		resultXml.append("<insert>"+pidOrFilename+"</insert>\n");
        logger.info("indexDoc="+pidOrFilename+" docCount="+docCount);
	}

}