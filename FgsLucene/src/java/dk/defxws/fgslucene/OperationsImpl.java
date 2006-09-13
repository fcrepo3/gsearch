//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file will be subject to the
 * same open source license as the Fedora Repository System at www.fedora.info
 * It is expected to be released with Fedora version 2.2.
 *
 * <p>The entire file consists of original code.  
 * Copyright &copy; 2006 by The Technical University of Denmark.
 * All rights reserved.</p>
 *
 */
package dk.defxws.fgslucene;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.TreeSet;

import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexModifier;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import dk.defxws.fedoragsearch.server.GTransformer;
import dk.defxws.fedoragsearch.server.GenericOperationsImpl;
import dk.defxws.fedoragsearch.server.errors.FedoraObjectNotFoundException;
import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

/**
 * performs the Lucene specific parts of the operations
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class OperationsImpl extends GenericOperationsImpl {
    
    private static final Logger logger = Logger.getLogger(OperationsImpl.class);
    
    private IndexModifier modifier = null;
    
    public String gfindObjects(
            String query,
            int hitPageStart,
            int hitPageSize,
            int snippetsMax,
            int fieldMaxLength,
            String indexName,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        super.gfindObjects(query, hitPageStart, hitPageSize, snippetsMax, fieldMaxLength, indexName, resultPageXslt);
        ResultSet resultSet = (new Connection()).createStatement().executeQuery(
                query,
                hitPageStart,
                hitPageSize,
                snippetsMax,
                fieldMaxLength,
                getAnalyzer(config.getAnalyzer(indexName)),
                config.getDefaultQueryFields(indexName),
                config.getIndexDir(indexName),
                config.getIndexName(indexName));
        if (logger.isDebugEnabled())
            logger.debug("resultSet.getResultXml()="+resultSet.getResultXml());
        params[10] = "RESULTPAGEXSLT";
        params[11] = resultPageXslt;
        String xsltPath = config.getConfigName()+"/index/"+config.getIndexName(indexName)+"/"+config.getGfindObjectsResultXslt(indexName, resultPageXslt);
        StringBuffer sb = (new GTransformer()).transform(
        		xsltPath,
                resultSet.getResultXml(),
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
        StringBuffer resultXml = new StringBuffer("<fields>");
        int termNo = 0;
        IndexReader ir = null;
        try {
            ir = IndexReader.open(config.getIndexDir(indexName));
            Iterator fieldNames = (new TreeSet(ir.getFieldNames(IndexReader.FieldOption.INDEXED))).iterator();
            while (fieldNames.hasNext()) {
                resultXml.append("<field>"+fieldNames.next()+"</field>");
            }
            resultXml.append("</fields>");
            resultXml.append("<terms>");
            int pageSize = 0;
            Term beginTerm = new Term(fieldName, "");
            TermEnum terms;
            try {
                terms = ir.terms(beginTerm);
            } catch (IOException e) {
                throw new GenericSearchException("IndexReader terms error:\n" + e.toString());
            }
            try {
                while (terms.term()!=null && terms.term().field().equals(fieldName)
                        && !"".equals(terms.term().text().trim())) {
                    termNo++;
                    if (startTerm.compareTo(terms.term().text()) <= 0 && pageSize < termPageSize) {
                        pageSize++;
                        resultXml.append("<term no=\""+termNo+"\""
                                +" fieldtermhittotal=\""+terms.docFreq()
                                +"\">"+terms.term().text()+"</term>");
                    }
                    terms.next();
                }
            } catch (IOException e) {
                throw new GenericSearchException("IndexReader terms.next error:\n" + e.toString());
            }
            try {
                terms.close();
            } catch (IOException e) {
                throw new GenericSearchException("IndexReader terms close error:\n" + e.toString());
            }
        } catch (IOException e) {
            throw new GenericSearchException("IndexReader new error:\n" + e.toString());
        } finally {
            if (ir!=null) 
                try {
                    ir.close();
                } catch (IOException e) {
                }
        }
        resultXml.append("</terms>");
        resultXml.insert(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<lucenebrowseindex "+
                "   xmlns:dc=\"http://purl.org/dc/elements/1.1/"+
                "\" startTerm=\""+startTerm+
                "\" termPageSize=\""+termPageSize+
                "\" fieldName=\""+fieldName+
                "\" indexName=\""+indexName+
                "\" termTotal=\""+termNo+"\">");
        resultXml.append("</lucenebrowseindex>");
        if (logger.isDebugEnabled())
            logger.debug("resultXml="+resultXml);
        params[10] = "RESULTPAGEXSLT";
        params[11] = resultPageXslt;
        String xsltPath = config.getConfigName()+"/index/"+config.getIndexName(indexName)+"/"+config.getBrowseIndexResultXslt(indexName, resultPageXslt);
        StringBuffer sb = (new GTransformer()).transform(
        		xsltPath,
                resultXml,
                params);
        return sb.toString();
    }
    
    public String getIndexInfo(
            String indexName,
            String resultPageXslt)
    throws java.rmi.RemoteException {
        super.getIndexInfo(indexName, resultPageXslt);
        InputStream infoStream =  null;
        String indexInfoPath = "/"+config.getConfigName()+"/index/"+config.getIndexName(indexName)+"/indexInfo.xml";
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
        resultXml.append("<luceneUpdateIndex");
        resultXml.append(" indexName=\""+indexName+"\"");
        resultXml.append(">\n");
        try {
            if ("createEmpty".equals(action)) 
                createEmpty(config.getIndexDir(indexName), resultXml);
            else {
                try {
                    modifier = new IndexModifier(config.getIndexDir(indexName), getAnalyzer(config.getAnalyzer(indexName)), false);
                } catch (IOException e) {
                    if (e.toString().indexOf("/segments")>-1) {
                        try {
                            modifier = new IndexModifier(config.getIndexDir(indexName), getAnalyzer(config.getAnalyzer(indexName)), true);
                        } catch (IOException e2) {
                            throw new GenericSearchException("IndexModifier new error, creating index\n", e2);
                        }
                    }
                    else
                        throw new GenericSearchException("IndexModifier new error\n", e);
                }
                if ("fromFoxmlFiles".equals(action)) 
                    fromFoxmlFiles(value, repositoryName, indexName, resultXml, indexDocXslt);
                else
                    if ("fromPid".equals(action)) 
                        fromPid(value, repositoryName, indexName, resultXml, indexDocXslt);
                    else
                        if ("deletePid".equals(action)) 
                            deletePid(value, resultXml);
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
        if (modifier != null) {
            try {
                modifier.optimize();
            } catch (IOException e) {
                throw new GenericSearchException("IndexModifier optimize error", e);
            }
            docCount = modifier.docCount();
            try {
                modifier.close();
            } catch (IOException e) {
                throw new GenericSearchException("IndexModifier close error", e);
            }
        }
        resultXml.append("<counts");
        resultXml.append(" insertTotal=\""+insertTotal+"\"");
        resultXml.append(" updateTotal=\""+updateTotal+"\"");
        resultXml.append(" deleteTotal=\""+deleteTotal+"\"");
        resultXml.append(" docCount=\""+docCount+"\"");
        resultXml.append("/>\n");
        resultXml.append("</luceneUpdateIndex>\n");
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
        try {
            modifier = new IndexModifier(filePath, new StandardAnalyzer(), true);
        } catch (IOException e) {
            throw new GenericSearchException("IndexModifier new error", e);
        }
        resultXml.append("<createEmpty/>\n");
    }
    
    private void fromFoxmlFiles(
            String filePath,
            String repositoryName,
            String indexName,
            StringBuffer resultXml,
            String indexDocXslt)
    throws java.rmi.RemoteException {
        if (logger.isDebugEnabled())
            logger.debug("fromFoxmlFiles filePath="+filePath+" repositoryName="+repositoryName+" indexName="+indexName);
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
        if (logger.isDebugEnabled())
            logger.debug("indexDocs file="+file+" repositoryName="+repositoryName+" indexName="+indexName);
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
        resultXml.append("<docCount>"+modifier.docCount()+"</docCount>\n");
    }
    
    private void fromPid(
            String pid,
            String repositoryName,
            String indexName,
            StringBuffer resultXml,
            String indexDocXslt)
    throws java.rmi.RemoteException {
        try {
			getFoxmlFromPid(pid, repositoryName);
		} catch (java.rmi.RemoteException e) {
			throw new FedoraObjectNotFoundException("Fedora Object "+pid+" not found at "+repositoryName);
		}
        indexDoc(pid, repositoryName, indexName, new ByteArrayInputStream(foxmlRecord), resultXml, indexDocXslt);
    }
    
    private void deletePid(
            String pid,
            StringBuffer resultXml)
    throws java.rmi.RemoteException {
        try {
            int deleted = modifier.deleteDocuments(new Term("PID", pid));
            deleteTotal += deleted;
            docCount = modifier.docCount();
            logger.info("indexDoc="+pid+" docCount="+docCount);
        } catch (IOException e) {
            throw new GenericSearchException("Update deletePid error pid="+pid, e);
        }
    }
    
    private void indexDoc(
            String pidOrFilename,
            String repositoryName,
            String indexName,
            InputStream foxmlStream,
            StringBuffer resultXml,
            String indexDocXslt)
    throws java.rmi.RemoteException {
        IndexDocumentHandler hdlr = null;
        String xsltPath = config.getConfigName()+"/index/"+indexName+"/"+config.getUpdateIndexDocXslt(indexName, indexDocXslt);
        StringBuffer sb = (new GTransformer()).transform(
        		xsltPath, 
                new StreamSource(foxmlStream),
                new String[] {});
        if (logger.isDebugEnabled())
            logger.debug("indexDoc=\n"+sb.toString());
        hdlr = new IndexDocumentHandler(
                this,
                repositoryName,
                pidOrFilename,
                sb);
        try {
            int deleted = 0;
            if (!(hdlr.getPid()==null || hdlr.getPid().equals("")))
                deleted = modifier.deleteDocuments(new Term("PID", hdlr.getPid()));
            deleteTotal += deleted;
            if (hdlr.getIndexDocument().fields().hasMoreElements()) {
                hdlr.getIndexDocument().add(new Field("repositoryName", repositoryName, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
                modifier.addDocument(hdlr.getIndexDocument());
                modifier.flush();
                resultXml.append("<insert>"+hdlr.getPid()+"</insert>\n");
                resultXml.append("<docCount>"+modifier.docCount()+"</docCount>\n");
                if (deleted>0) {
                    updateTotal++;
                    deleteTotal-= deleted;
                }
                else insertTotal++;
                logger.info("indexDoc="+hdlr.getPid()+" docCount="+modifier.docCount());
            }
        } catch (IOException e) {
            throw new GenericSearchException("Update error pidOrFilename="+pidOrFilename, e);
        }
    }
    
    public Analyzer getAnalyzer(String analyzerClassName)
    throws GenericSearchException {
        Analyzer analyzer = null;
        if (logger.isDebugEnabled())
            logger.debug("analyzerClassName=" + analyzerClassName);
        try {
            Class analyzerClass = Class.forName(analyzerClassName);
            if (logger.isDebugEnabled())
                logger.debug("analyzerClass=" + analyzerClass.toString());
            analyzer = (Analyzer) analyzerClass.getConstructor(new Class[] {})
            .newInstance(new Object[] {});
            if (logger.isDebugEnabled())
                logger.debug("analyzer=" + analyzer.toString());
        } catch (ClassNotFoundException e) {
            throw new GenericSearchException(analyzerClassName
                    + ": class not found.\n", e);
        } catch (InstantiationException e) {
            throw new GenericSearchException(analyzerClassName
                    + ": instantiation error.\n", e);
        } catch (IllegalAccessException e) {
            throw new GenericSearchException(analyzerClassName
                    + ": instantiation error.\n", e);
        } catch (InvocationTargetException e) {
            throw new GenericSearchException(analyzerClassName
                    + ": instantiation error.\n", e);
        } catch (NoSuchMethodException e) {
            throw new GenericSearchException(analyzerClassName
                    + ": instantiation error.\n", e);
        }
        return analyzer;
    }
    
}