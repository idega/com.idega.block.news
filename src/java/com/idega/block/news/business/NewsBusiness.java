package com.idega.block.news.business;



import java.sql.*;

import com.idega.presentation.IWContext;

import com.idega.block.news.data.*;

import com.idega.block.text.data.LocalizedText;

import com.idega.block.text.business.*;

import com.idega.block.category.business.*;

import com.idega.core.data.ICCategory;

import com.idega.core.data.ICObjectInstance;

import com.idega.util.IWTimeStamp;

import com.idega.core.data.ICFile;

import java.util.List;

import java.util.Vector;

import java.util.Iterator;

import com.idega.data.EntityFinder;

import com.idega.block.text.data.Content;



public class NewsBusiness{



  public static NwNews getNews(int iNewsId){

    NwNews NW = ((com.idega.block.news.data.NwNewsHome)com.idega.data.IDOLookup.getHomeLegacy(NwNews.class)).createLegacy();

    if ( iNewsId > 0 ) {

      try {

       NW = ((com.idega.block.news.data.NwNewsHome)com.idega.data.IDOLookup.getHomeLegacy(NwNews.class)).findByPrimaryKeyLegacy(iNewsId);

      }

      catch (SQLException e) {

        e.printStackTrace();

        NW = ((com.idega.block.news.data.NwNewsHome)com.idega.data.IDOLookup.getHomeLegacy(NwNews.class)).createLegacy();

      }

    }

    else {

      NW =  null;

    }

    return NW;

  }



  public static boolean disconnectBlock(int instanceid){

    return CategoryBusiness.getInstance().disconnectBlock(instanceid);

  }



  public static boolean disconnectNewsCategory(NewsCategory newsCat,int iObjectInstanceId){

    try {

      //newsCat.setValid(false);

      //newsCat.update();

      if(iObjectInstanceId > 0  ){

        ICObjectInstance obj = ((com.idega.core.data.ICObjectInstanceHome)com.idega.data.IDOLookup.getHomeLegacy(ICObjectInstance.class)).findByPrimaryKeyLegacy(iObjectInstanceId);

        newsCat.removeFrom(obj);

      }



      return true;

    }

    catch (SQLException ex) {



    }

    return false;

  }





  public static boolean deleteBlock(int instanceid){

    List L = NewsFinder.listOfNewsCategoryForObjectInstanceId(instanceid);

    if(L!= null){

      Iterator I = L.iterator();

      while(I.hasNext()){

        NewsCategory N = (NewsCategory) I.next();

        deleteNewsCategory(N.getID(),instanceid );

      }

      return true;

    }

    else

      return false;

  }



  public static void deleteNewsCategory(int iNewsCategoryId){

    deleteNewsCategory(iNewsCategoryId ,NewsFinder.getObjectInstanceIdFromNewsCategoryId(iNewsCategoryId));

  }



  private static void deleteNwNews(NwNews nwNews) throws SQLException{

    int contentId = nwNews.getContentId();

    nwNews.delete();

    ContentBusiness.deleteContent(contentId);

  }



  public static boolean deleteNews(int iNewsId){

    try {

      deleteNwNews(((com.idega.block.news.data.NwNewsHome)com.idega.data.IDOLookup.getHomeLegacy(NwNews.class)).findByPrimaryKeyLegacy(iNewsId ));

      return true;

    }

    catch (SQLException ex) {

      return false;

    }

  }



	public static void moveNewsBetweenCategories(int fromCategoryId,int toCategoryId){

	  if(fromCategoryId > 0 && toCategoryId > 0){

			NwNews news = (NwNews) com.idega.block.news.data.NwNewsBMPBean.getStaticInstance(NwNews.class);

		  StringBuffer sql = new StringBuffer("update ");

			sql.append(news.getEntityName());

			sql.append(" set ");

			sql.append(com.idega.block.news.data.NwNewsBMPBean.getColumnNameNewsCategoryId());

			sql.append(" = ");

			sql.append(toCategoryId);

			sql.append(" where ");

			sql.append(com.idega.block.news.data.NwNewsBMPBean.getColumnNameNewsCategoryId());

			sql.append(" = ");

			sql.append(fromCategoryId);

			//System.err.println(sql.toString());

			try {



				com.idega.data.SimpleQuerier.execute(sql.toString());

			}

			catch (Exception ex) {

				ex.printStackTrace();

			}

		}

	}



  public static void deleteNewsCategory(int iCategoryId ,int iObjectInstanceId) {

    javax.transaction.TransactionManager t = com.idega.transaction.IdegaTransactionManager.getInstance();

    try {

      t.begin();

    //  List O = TextFinder.listOfObjectInstanceTexts();

      NewsCategory nc = ((com.idega.block.news.data.NewsCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(NewsCategory.class)).findByPrimaryKeyLegacy( iCategoryId );

      List L = NewsFinder.listOfNwNewsInCategory(nc.getID());

      if(L != null){

        NwNews news;



        for (int i = 0; i < L.size(); i++) {

          news = (NwNews) L.get(i);

          deleteNwNews(news);

        }

      }



      if(iObjectInstanceId > 0  ){

        ICObjectInstance obj = ((com.idega.core.data.ICObjectInstanceHome)com.idega.data.IDOLookup.getHomeLegacy(ICObjectInstance.class)).findByPrimaryKeyLegacy(iObjectInstanceId);

        nc.removeFrom(obj);

      }

      nc.delete();

     t.commit();

    }

    catch(Exception e) {

      try {

        t.rollback();

      }

      catch(javax.transaction.SystemException ex) {

        ex.printStackTrace();

      }

      e.printStackTrace();

    }

  }



  public static NwNews saveNews(int iNwNewsId,int iLocalizedTextId,int iCategoryId,

            String sHeadline,String sTitle,String sAuthor,String sSource,String sBody,

            int iLocaleId ,int iUserId,int InstanceId,

            Timestamp tsPubFrom,Timestamp tsPubTo,List listOfFiles){





    try {

      boolean update = false;

		  NwNews eNwNews = ((com.idega.block.news.data.NwNewsHome)com.idega.data.IDOLookup.getHomeLegacy(NwNews.class)).createLegacy();

      if(iNwNewsId > 0){

        eNwNews = ((com.idega.block.news.data.NwNewsHome)com.idega.data.IDOLookup.getHomeLegacy(NwNews.class)).findByPrimaryKeyLegacy(iNwNewsId);

        update = true;

      }

      Content eContent = ContentBusiness.saveContent(eNwNews.getContentId(),iLocalizedTextId,iLocaleId,iUserId,tsPubFrom,tsPubTo,sHeadline,sBody,sTitle,listOfFiles);

      if(eContent != null){

        if(eContent.getID() > 0)

          eNwNews.setContentId(eContent.getID());

        if(sAuthor != null)

          eNwNews.setAuthor(sAuthor );

        if(sSource !=null)

          eNwNews.setSource(sSource );



        eNwNews.setNewsCategoryId(iCategoryId );

        if(update)

        eNwNews.update();

        else

          eNwNews.insert();

        return eNwNews;

      }

    }

    catch (SQLException ex) {

      ex.printStackTrace();

    }

    return null;

  }

    /*

    javax.transaction.TransactionManager t = com.idega.transaction.IdegaTransactionManager.getInstance();

    try {

      t.begin();

      boolean nwUpdate = false;

      boolean locUpdate = false;

      NwNews nwNews = null;

      List files = null;

      LocalizedText locText = null;

      if(iNwNewsId > 0){

        nwUpdate = true;

        nwNews = ((com.idega.block.news.data.NwNewsHome)com.idega.data.IDOLookup.getHomeLegacy(NwNews.class)).findByPrimaryKeyLegacy(iNwNewsId);

        files = NewsFinder.listOfNewsFiles(nwNews);

        if(iLocalizedTextId > 0){

          locUpdate = true;

          locText = ((com.idega.block.text.data.LocalizedTextHome)com.idega.data.IDOLookup.getHomeLegacy(LocalizedText.class)).findByPrimaryKeyLegacy(iLocalizedTextId);

        }

        else{

          locUpdate = false;

          locText = ((com.idega.block.text.data.LocalizedTextHome)com.idega.data.IDOLookup.getHomeLegacy(LocalizedText.class)).createLegacy();

        }

      }

      else{

        nwUpdate = false;

        locUpdate = false;

        nwNews = ((com.idega.block.news.data.NwNewsHome)com.idega.data.IDOLookup.getHomeLegacy(NwNews.class)).createLegacy();

        locText = ((com.idega.block.text.data.LocalizedTextHome)com.idega.data.IDOLookup.getHomeLegacy(LocalizedText.class)).createLegacy();

        files = (List)new Vector();

      }



      locText.setHeadline(sHeadline);

      locText.setBody(sBody);

      locText.setLocaleId(iLocaleId);

      locText.setTitle( sTitle);

      locText.setUpdated(IWTimeStamp.getTimestampRightNow());





      if(iImageId > 0){

        ICFile file = ((com.idega.core.data.ICFileHome)com.idega.data.IDOLookup.getHomeLegacy(ICFile.class)).findByPrimaryKeyLegacy(iImageId);

        ICFile[] nwfile = (ICFile[]) nwNews.findRelated(file);

        if(nwfile == null || nwfile.length == 0){

          file.addTo(nwNews );

        }

        else

          System.err.println(" not adding file to news");

      }

      nwNews.setImageId(iImageId);

      nwNews.setIncludeImage(useImage);

      nwNews.setUpdated(IWTimeStamp.getTimestampRightNow());

      nwNews.setNewsCategoryId(iCategoryId );

      nwNews.setAuthor(sAuthor);

      nwNews.setSource(sSource);

      nwNews.setPublishFrom(pubFrom.getTimestamp());

      nwNews.setPublishTo(pubTo.getTimestamp());



      if(nwUpdate ){

        nwNews.update();

        if(locUpdate){

          locText.update();

        }

        else if(!locUpdate){

          locText.setCreated(IWTimeStamp.getTimestampRightNow());

          locText.insert();

          locText.addTo(nwNews);

        }

      }

      else if(!nwUpdate){

        nwNews.setNewsDate(IWTimeStamp.getTimestampRightNow());

        nwNews.setCreated(IWTimeStamp.getTimestampRightNow());

        nwNews.setUserId(iUserId);

        nwNews.insert();

        locText.setCreated(IWTimeStamp.getTimestampRightNow());

        locText.insert();

        locText.addTo(nwNews);

        if(InstanceId > 0){

          ICObjectInstance objIns = ((com.idega.core.data.ICObjectInstanceHome)com.idega.data.IDOLookup.getHomeLegacy(ICObjectInstance.class)).findByPrimaryKeyLegacy(InstanceId);

          nwNews.addTo(objIns);

        }

      }

      t.commit();

    }

    catch(Exception e) {

      try {

        t.rollback();

      }

      catch(javax.transaction.SystemException ex) {

        ex.printStackTrace();

      }

      e.printStackTrace();

    }





  }

*/

  public static ICCategory saveNewsCategory(int iCategoryId,String sName,String sDesc,int iObjectInstanceId){

    return CategoryBusiness.getInstance().saveCategory(iCategoryId,sName,sDesc,iObjectInstanceId,((com.idega.block.news.data.NewsCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(NewsCategory.class)).createLegacy().getCategoryType());

  }

/*

  public static NewsCategory saveNewsCategory(int iCategoryId,String sName,String sDesc,int iObjectInstanceId){

    javax.transaction.TransactionManager t = com.idega.transaction.IdegaTransactionManager.getInstance();

    try{

     t.begin();

      boolean update = false;

      NewsCategory newsCat = null;

      if(iCategoryId > 0){

        update = true;

        newsCat = ((com.idega.block.news.data.NewsCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(NewsCategory.class)).findByPrimaryKeyLegacy(iCategoryId );

      }

      else{

        newsCat = ((com.idega.block.news.data.NewsCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(NewsCategory.class)).createLegacy();

      }



      newsCat.setName(sName);

      newsCat.setDescription(sDesc);

      newsCat.setValid(true);





      if(update){

        newsCat.update();

      }

      else{

        newsCat.insert();

      }



			// Binding category to instanceId

			if(iObjectInstanceId > 0){

				ICObjectInstance objIns = ((com.idega.core.data.ICObjectInstanceHome)com.idega.data.IDOLookup.getHomeLegacy(ICObjectInstance.class)).findByPrimaryKeyLegacy(iObjectInstanceId);

				// Allows only one category per instanceId

				objIns.removeFrom(((com.idega.block.news.data.NewsCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(NewsCategory.class)).createLegacy());

        newsCat.addTo(objIns);

      }



      t.commit();

      return newsCat;

    }

    catch(Exception e) {

      try {

        t.rollback();

      }

      catch(javax.transaction.SystemException ex) {

        ex.printStackTrace();

      }

      e.printStackTrace();

    }

    return null;

  }

*/

  public static int createNewsCategory(int iObjectInstanceId){

    return saveNewsCategory(-1,"News","News",iObjectInstanceId ).getID();

  }

}



