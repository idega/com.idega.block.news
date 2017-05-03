package com.idega.block.news.data;



//import java.util.*;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

import javax.ejb.FinderException;

import com.idega.block.category.data.ICCategory;
import com.idega.block.text.data.Content;
import com.idega.block.text.data.ContentBMPBean;
import com.idega.block.text.data.LocalizedText;
import com.idega.block.text.data.LocalizedTextBMPBean;
import com.idega.core.file.data.ICFile;
import com.idega.data.IDORelationshipException;
import com.idega.data.query.AND;
import com.idega.data.query.Column;
import com.idega.data.query.InCriteria;
import com.idega.data.query.MatchCriteria;
import com.idega.data.query.OR;
import com.idega.data.query.SelectQuery;
import com.idega.data.query.Table;
import com.idega.util.IWTimestamp;



/**

 * Title:

 * Description:

 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved

 * Company:      idega

  *@author <a href="mailto:aron@idega.is">Aron Birkir</a>

 * @version 1.1

 */



public class NwNewsBMPBean extends com.idega.data.GenericEntity implements  NwNews{



  public NwNewsBMPBean(){

          super();

  }

  public NwNewsBMPBean(int id)throws SQLException{

          super(id);

  }

  @Override
public void initializeAttributes(){

    addAttribute(getIDColumnName());

    addAttribute(getColumnNameContentId(), "Content", true, true, Integer.class,"many-to-one",Content.class);

    addAttribute(getColumnNameNewsCategoryId(), "Category", true, true, Integer.class, "many-to-one",ICCategory.class);

    addAttribute(getColumnNameAuthor(), "Author", true, true, String.class);

    addAttribute(getColumnNameSource(), "Source", true, true, String.class);

    addIndex("IDX_NW_NEWS_1", getColumnNameContentId());
    addIndex("IDX_NW_NEWS_2", getColumnNameNewsCategoryId());
  }

  @Override
public String getEntityName(){

    return getEntityTableName();

  }

  public static String getEntityTableName(){return "NW_NEWS";}



  public static String getColumnNameNewsCategoryId(){return "IC_CATEGORY_ID";}

  public static String getColumnNameContentId(){ return "CONTENT_ID";}

  public static String getColumnNameAuthor(){return "AUTHOR";}

  public static String getColumnNameSource(){return "SOURCE";}



  @Override
public void setDefaultValues() {

    this.setNewsCategoryId(1);

    this.setSource("");

    this.setAuthor("");

  }





  @Override
public int getNewsCategoryId(){

    return getIntColumnValue(getColumnNameNewsCategoryId());

  }

  @Override
public ICCategory getNewsCategory() {
  	return (ICCategory)getColumnValue(getColumnNameNewsCategoryId());
  }

  @Override
public void setNewsCategoryId(Integer news_category_id){

    setColumn(getColumnNameNewsCategoryId(), news_category_id);

  }

  @Override
public void setNewsCategoryId(int news_category_id){

    setColumn(getColumnNameNewsCategoryId(), new Integer(news_category_id));

  }

  @Override
public int getContentId(){

    return getIntColumnValue(getColumnNameContentId());

  }

  @Override
public void setContentId(int iContentId){

    setColumn(getColumnNameContentId(),iContentId);

  }

  @Override
public void setContentId(Integer iContentId){

    setColumn(getColumnNameContentId(),iContentId);

  }

  @Override
public String getAuthor(){

    return getStringColumnValue(getColumnNameAuthor());

  }

  @Override
public void setAuthor(String author){

    setColumn(getColumnNameAuthor(), author);

  }

  @Override
public String getSource(){

    return getStringColumnValue(getColumnNameSource());

  }

  @Override
public void setSource(String source){

    setColumn(getColumnNameSource(), source);

  }

  @Override
public Content getContent(){
  	return (Content) this.getColumnValue(getColumnNameContentId());
  }



  @Override
public Collection getRelatedFiles() throws IDORelationshipException{
  	return idoGetRelatedEntities(ICFile.class);
  }

  @Override
public Collection getLocalizedTexts() throws IDORelationshipException{
      return this.idoGetRelatedEntities(LocalizedText.class);
  }

  public Collection ejbFindPublishedByCategoriesAndLocale(int[] newsCategoryIds,int iLocaleId,boolean ignorePublishingDates,int maxNumberOfNews)throws FinderException{
      String middleTable = ((com.idega.block.text.data.ContentHome)com.idega.data.IDOLookup.getHomeLegacy(Content.class)).createLegacy().getLocalizedTextMiddleTableName(((com.idega.block.text.data.LocalizedTextHome)com.idega.data.IDOLookup.getHomeLegacy(LocalizedText.class)).createLegacy(),((com.idega.block.text.data.ContentHome)com.idega.data.IDOLookup.getHomeLegacy(Content.class)).createLegacy());
      Table news = new Table(com.idega.block.news.data.NwNewsBMPBean.getEntityTableName(), "n");
      Table content = new Table(com.idega.block.text.data.ContentBMPBean.getEntityTableName(), "c");
      Table text = new Table(com.idega.block.text.data.LocalizedTextBMPBean.getEntityTableName(), "t");
      Table middle = new Table(middleTable, "m");

      SelectQuery query = new SelectQuery(news);
      query.addColumn(new Column(news, getIDColumnName()));

      query.addJoin(news, NwNewsBMPBean.getColumnNameContentId(), content, ContentBMPBean.getEntityTableName()+"_ID");
      query.addJoin(content, ContentBMPBean.getEntityTableName()+"_ID", middle, ContentBMPBean.getEntityTableName()+"_ID");
      query.addJoin(middle, LocalizedTextBMPBean.getEntityTableName()+"_ID", text, LocalizedTextBMPBean.getEntityTableName()+"_ID");

      query.addCriteria(new InCriteria(news, NwNewsBMPBean.getColumnNameNewsCategoryId(), newsCategoryIds));
      query.addCriteria(new MatchCriteria(text, LocalizedTextBMPBean.getColumnNameLocaleId(), MatchCriteria.EQUALS, iLocaleId));
      if (!ignorePublishingDates ) {
      	IWTimestamp today = IWTimestamp.RightNow();
      	MatchCriteria from = new MatchCriteria(content, ContentBMPBean.getColumnNamePublishFrom(), MatchCriteria.LESSEQUAL, today.getTimestamp());
      	MatchCriteria toDate = new MatchCriteria(content, ContentBMPBean.getColumnNamePublishTo(), MatchCriteria.GREATEREQUAL, today.getTimestamp());
      	MatchCriteria toIsNull = new MatchCriteria(content, ContentBMPBean.getColumnNamePublishTo(), MatchCriteria.IS, MatchCriteria.NULL);
      	OR to = new OR(toDate,toIsNull);
      	query.addCriteria(new AND(from, to));
      }
      query.addOrder(content, ContentBMPBean.getColumnNameCreated(), false);

      try {
    	  return idoFindPKsByQuery(query, maxNumberOfNews);
      } catch (Exception e) {
    	  getLogger().log(Level.WARNING, "Error executing query: " + query, e);
      }
      return Collections.emptyList();
  }

  public Collection ejbFindPublishedByCategory(int newsCategoryId,boolean ignorePublishingDates)throws FinderException{
      Table news = new Table(com.idega.block.news.data.NwNewsBMPBean.getEntityTableName(), "n");
      Table content = new Table(com.idega.block.text.data.ContentBMPBean.getEntityTableName(), "c");

      SelectQuery query = new SelectQuery(news);
      query.addColumn(new Column(news, getIDColumnName()));

      query.addJoin(news, NwNewsBMPBean.getColumnNameContentId(), content, ContentBMPBean.getEntityTableName()+"_ID");
      query.addCriteria(new MatchCriteria(news, NwNewsBMPBean.getColumnNameNewsCategoryId(), MatchCriteria.EQUALS, newsCategoryId));
      if (!ignorePublishingDates ) {
      	IWTimestamp today = IWTimestamp.RightNow();
        	MatchCriteria from = new MatchCriteria(content, ContentBMPBean.getColumnNamePublishFrom(), MatchCriteria.LESSEQUAL, today.getTimestamp());
        	MatchCriteria to = new MatchCriteria(content, ContentBMPBean.getColumnNamePublishTo(), MatchCriteria.GREATEREQUAL, today.getTimestamp());
        	query.addCriteria(new AND(from, to));
      }
      query.addOrder(content, ContentBMPBean.getColumnNameCreated(), false);
      return idoFindPKsByQuery(query);
}

}
