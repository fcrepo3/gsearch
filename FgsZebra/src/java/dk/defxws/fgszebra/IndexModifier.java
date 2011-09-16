//$Id$
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008, 2009, 2010, 2011 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fgszebra;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

/**
 * modifies the Zebra index 
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class IndexModifier {

	private static final Logger logger =
		Logger.getLogger(IndexModifier.class);
	private String indexDir;
	private static Hashtable oneIndexModifierPerIndexDir = new Hashtable();

	/**
	 */
	public IndexModifier(String indexDir, boolean create) throws GenericSearchException {
		if (null != oneIndexModifierPerIndexDir.get(indexDir)) {
			throw new GenericSearchException("Index modification busy on indexDir="+indexDir);
		}
		oneIndexModifierPerIndexDir.put(indexDir, this);
		this.indexDir = indexDir;
		if (create) {

		}
	}

	private void init() throws GenericSearchException {
	}

	public void runCreateEmpty(
			StringBuffer resultXml)
	throws java.rmi.RemoteException {
		try {
			Process p = Runtime.getRuntime().exec("sh "+indexDir+"/runupdate.sh createEmpty 2>&1 &", null, new File(indexDir));
		} catch (IOException e) {
			throw new GenericSearchException("Error createEmpty indexDir="+indexDir, e);
		}
		resultXml.append("<createEmpty/>\n");
		(new File(indexDir+"counts")).delete();
	}

	/**
	 */
	public int[] runUpdate(StringBuffer doc, String action, String dir) 
	throws GenericSearchException {
		int[] opcounts = {0, 0, 0};
		try {
			if (doc!=null) {
				FileWriter fw = new FileWriter(dir+"/temp_records/rec");
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(doc.toString());
				bw.close();
			}
			Process pt = Runtime.getRuntime().exec("tail -f -n 1 indexer.log &", null, new File(dir));
			Process p = Runtime.getRuntime().exec("sh "+dir+"/runupdate.sh "+action+" 2>&1 &", null, new File(dir));
			InputStream is = pt.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.indexOf("Records:") > -1 || line.indexOf("temp_records/sb") > -1)
					if (logger.isDebugEnabled())
						logger.debug("runupdate line="+line);
				int i = line.indexOf("Records:");
				if (i  > -1) {
					int j = line.indexOf("i/u/d");
					if (j  > -1) {
						StringTokenizer sto = new StringTokenizer(line.substring(j+6), "/");
						opcounts[0] = Integer.parseInt(sto.nextToken().trim());
						opcounts[1] = Integer.parseInt(sto.nextToken().trim());
						opcounts[2] = Integer.parseInt(sto.nextToken().trim());
					}
					break;
				}
			}
			br.close();
			pt.destroy();
		} catch (IOException e) {
			throw new GenericSearchException("runupdate error action="+action+" dir="+dir, e);
		}
		return opcounts;
	}

	/**
	 */
	void setCounts(int[] counts) throws GenericSearchException {
		String line = counts[0]+"/"+counts[1]+"/"+counts[2]+"\n";
		if (logger.isInfoEnabled())
			logger.info("setCounts="+line);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(indexDir+"/counts"));
			bw.write(line);
			bw.close();
		} catch (IOException e) {
			throw new GenericSearchException("setCounts error ", e);
		}
	}

	/**
	 */
	int[] getCounts() throws GenericSearchException {
		int[] counts = {0, 0, 0};
		String countsline = "0/0/0";
		try {
			BufferedReader br = new BufferedReader(new FileReader(indexDir+"/counts"));
			countsline = br.readLine();
			br.close();
		} catch (FileNotFoundException e) {
			throw new GenericSearchException("getCounts error ", e);
		} catch (IOException e) {
			throw new GenericSearchException("getCounts error ", e);
		}
		if (logger.isInfoEnabled())
			logger.info("getCounts="+countsline);
		if (countsline == null || countsline.indexOf("/") == -1) {
			return counts;
		}
		StringTokenizer st = new StringTokenizer(countsline, "/");
		counts[0] = Integer.parseInt(st.nextToken().trim());
		counts[1] = Integer.parseInt(st.nextToken().trim());
		counts[2] = Integer.parseInt(st.nextToken().trim());
		return counts;
	}

	/**
	 */
	void close() throws GenericSearchException {
		oneIndexModifierPerIndexDir.remove(indexDir);
	}

}
