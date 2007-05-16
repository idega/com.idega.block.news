package com.idega.block.news.presentation;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import com.idega.block.news.business.NewsFinder;
import com.idega.block.news.business.NewsHelper;
import com.idega.block.news.data.NwNews;
import com.idega.block.text.business.ContentHelper;
import com.idega.block.text.data.LocalizedText;
import com.idega.core.file.data.ICFile;
import com.idega.idegaweb.block.presentation.ImageWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.util.IWTimestamp;
import com.idega.util.text.TextSoap;

public class NewsReaderCSS extends NewsReader {

	private final static String ATTRIBUTE_AUTHOR = "author";
	private final static String ATTRIBUTE_CATEGORY = "category";
	private final static String ATTRIBUTE_CREATION_DATE = "creation_date";
	private final static String ATTRIBUTE_HEADLINE = "headline";
	private final static String ATTRIBUTE_TEASER = "teaser";
	private final static String ATTRIBUTE_SOURCE = "source";
	private final static String ATTRIBUTE_BODY = "body";
	private final static String ATTRIBUTE_IMAGE = "image";
	private final static String styleClassPrefix = "article_";
	private final static String DEFAULT_STYLE_CLASS = styleClassPrefix + "item";

	public NewsReaderCSS() {
		super();
	}

	public NewsReaderCSS(int iCategoryId) {
		super(iCategoryId);
	}

	protected boolean isCacheable(IWContext iwc) {
		return false;
	}

	protected PresentationObject publishNews(IWContext iwc, Locale locale, boolean collection) {
		List L = null;
		if (this.iLayout == COLLECTION_LAYOUT || collection) {
			L = NewsFinder.listOfAllNewsHelpersInCategory(getCategoryIds(), this.numberOfCollectionNews, locale);
		}
		else {
			L = NewsFinder.listOfNewsHelpersInCategory(getCategoryIds(), this.numberOfDisplayedNews, locale);
		}
		Layer l = new Layer();
		l.setStyleClass("newList");

		// int count = NewsFinder.countNewsInCategory(newsCategory.getID());
		// System.err.println(" news count "+count);
// boolean useDividedTable = this.iLayout == NEWS_SITE_LAYOUT ? true : false;
		if (L != null) {
			int len = Math.min(this.visibleNewsRangeEnd, L.size());
			Integer I;
			NewsHelper newsHelper;
			int startI = Math.max(0, (this.visibleNewsRangeStart - 1));
			for (int i = startI; i < len; i++) {
				if (this.numberOfExpandedNews == i) {
					collection = true; // show the rest as
				}
				// collection
				newsHelper = (NewsHelper) L.get(i);
				I = new Integer(i);
				if (this.objectsBetween != null && this.objectsBetween.containsKey(I)) {
					l.add((PresentationObject) this.objectsBetween.get(I));
					this.objectsBetween.remove(I);
				}

				Layer newsL = (Layer) getNewsTable(newsHelper, locale, false, collection, iwc, (i + 1) == len);
				if (i == startI) {
					newsL.setStyleClass("article_item_first");
				}

				if (i % 2 == 0) {
					newsL.setStyleClass("article_item_odd");
				}
				else {
					newsL.setStyleClass("article_item_even");
				}

				if (i == (len - 1)) {
					newsL.setStyleClass("article_item_last");
				}
				l.add(newsL);
			}
			// news collection
			if (this.showNewsCollectionButton) {
				if (!collection) {
					// adds collectionButton only if one category bound to instance:
					// if(getCategoryIds().length == 1)
					l.add(getCollectionTable(iwc, getCategoryIds()[0]));
				}
				else if (collection && isFromCollectionLink(iwc)) {
					l.add(getBackTable(iwc));
				}
				else if (collection && !isFromCollectionLink(iwc)) {
					l.add(getCollectionTable(iwc, getCategoryIds()[0]));
				}
			}
			// Finish objectsbetween
			if (this.objectsBetween != null && this.objectsBetween.size() > 0) {
				Vector V = new Vector(this.objectsBetween.values());
				Collections.reverse(V);
				Iterator iter = V.iterator();
				while (iter.hasNext()) {
					l.add((PresentationObject) iter.next());
				}
			}
		}
		return (l);
	}

	public PresentationObject getBackTable(IWContext iwc) {
		Table smallTable = new Table(1, 1);
		smallTable.setCellpadding(0);
		smallTable.setCellspacing(0);
		if (this.showBackButton) {
			smallTable.add(getBackLink(this.backImage), 1, 1);
			smallTable.add(Text.getNonBrakingSpace(), 1, 1);
		}
		if (this.showBackText) {
			Text tBack = new Text(this.iwrb.getLocalizedString("back", "Back"));
			tBack = setMoreAttributes(tBack);
			smallTable.add(getBackLink(tBack), 1, 1);
		}
		return smallTable;

	}

	protected PresentationObject getNewsTable(NewsHelper newsHelper, Locale locale, boolean showAll, boolean collection, IWContext iwc, boolean isLastNews) {
		Layer layer = new Layer();
		layer.setStyleClass(DEFAULT_STYLE_CLASS);

		ContentHelper contentHelper = newsHelper.getContentHelper();
		NwNews news = newsHelper.getNwNews();
		String categoryName = news.getNewsCategory().getName(locale);
		LocalizedText locText = contentHelper.getLocalizedText(locale);

		String sNewsBody = "";
		String sHeadline = "";
		String sTeaser = "";

		Layer authL = new Layer();
		authL.setStyleClass(styleClassPrefix + ATTRIBUTE_AUTHOR);
		String author = news.getAuthor();
		if (author != null) {
			authL.add(author);
		}
		layer.add(authL);

		Timestamp newsDate = newsHelper.getContentHelper().getContent().getCreated();
		Layer dateL = new Layer();
		dateL.setStyleClass(styleClassPrefix + ATTRIBUTE_CREATION_DATE);
		if (newsDate != null) {
			IWTimestamp s = new IWTimestamp(newsDate);
			if (dateFormat != null) {
				dateL.add(s.getDateString(dateFormat));
			}
			else {
				dateL.add(s.getLocaleDate(locale));
			}
		}
		layer.add(dateL);

		Layer sourceL = new Layer();
		sourceL.setStyleClass(styleClassPrefix + ATTRIBUTE_SOURCE);
		String source = news.getSource();
		if (source != null) {
			sourceL.add(source);
		}
		layer.add(sourceL);

		Layer categoryL = new Layer();
		categoryL.setStyleClass(styleClassPrefix + ATTRIBUTE_CATEGORY);
		if (categoryName != null) {
			sourceL.add(categoryName);
		}
		layer.add(categoryL);

		if (locText != null) {
			sHeadline = locText.getHeadline();
			sHeadline = sHeadline == null ? "" : sHeadline;
			sTeaser = locText.getTitle();
			sTeaser = sTeaser == null ? "" : sTeaser;
			sNewsBody = locText.getBody();
			sNewsBody = sNewsBody == null ? "" : sNewsBody;
		}

// boolean needMoreButton = collection;
// if (!showAll && this.numberOfHeadlineLetters > -1 && sHeadline.length() >= this.numberOfHeadlineLetters) {
// sHeadline = sHeadline.substring(0, this.numberOfHeadlineLetters) + "...";
// needMoreButton = true;
// }

		Layer headlineL = new Layer();
		headlineL.setStyleClass(styleClassPrefix + ATTRIBUTE_HEADLINE);
		Text headLine = new Text(sHeadline);
		if (this.headlineAsLink) {
			if (this.setHeadlineLinktToCategoryMainViewerPage) {
				headlineL.add(getLinkToCategoryMainViewerPage(headLine, news, iwc));
			}
			else {
				headlineL.add(getMoreLink(headLine, news.getID(), iwc));
			}
		}
		else {
			headlineL.add(headLine);
		}
		layer.add(headlineL);

		if (showTeaserText) {
			Layer teaserL = new Layer();
			teaserL.setStyleClass(styleClassPrefix + ATTRIBUTE_TEASER);
			teaserL.add(sTeaser);
			layer.add(teaserL);
		}

		if (!collection) {
			Layer bodyL = new Layer();
			bodyL.setStyleClass(styleClassPrefix + ATTRIBUTE_BODY);
			bodyL.add(sNewsBody);
			layer.add(bodyL);

			PresentationObject po = getNewsImage(newsHelper, sHeadline);
			if (po != null) {
				Layer imageL = new Layer();
				imageL.setStyleClass(styleClassPrefix + ATTRIBUTE_IMAGE);
				imageL.add(po);
				layer.add(imageL);
			}
		}

		// //////// MORE LINK ///////////////

		if (!showAll) {
			Layer l = new Layer();
			l.setStyleClass(styleClassPrefix + "MORE");
			Text tMore = new Text(iwrb.getLocalizedString("more", "More"));
			tMore = setMoreAttributes(tMore);
			l.add(getMoreLink(tMore, news.getID(), iwc));
			layer.add(l);
		}

		// ////////// ADMIN PART /////////////////////
		int ownerId = newsHelper.getContentHelper().getContent().getUserId();
		if (this.hasEdit || this.hasEditExisting || (this.hasAdd && (ownerId == iwc.getUserId()))) {
			layer.add(getNewsAdminPart(news, iwc));
		}

		return layer;
	}

	protected PresentationObject getNewsAdminPart(NwNews news, IWContext iwc) {
		Layer l = new Layer();
		l.setStyleClass(styleClassPrefix + "ADMIN");
		Link newsEdit = new Link(iwb.getImage("/shared/edit.gif"));
		newsEdit.setWindowToOpen(NewsEditorWindow.class);
		newsEdit.addParameter(NewsEditorWindow.prmNwNewsId, news.getID());
		newsEdit.addParameter(NewsEditorWindow.prmObjInstId, getICObjectInstanceID());

		Link newsDelete = new Link(this.iwb.getImage("/shared/delete.gif"));
		newsDelete.setWindowToOpen(NewsEditorWindow.class);
		newsDelete.addParameter(NewsEditorWindow.prmDelete, news.getID());

		l.add(newsEdit);
		l.add(newsDelete);
		return l;
	}

	protected PresentationObject getCollectionTable(IWContext iwc, int iCollectionCategoryId) {
		Layer l = new Layer();
		l.setStyleClass("collectionLinkLayer");
		if (this.collectionImage != null) {
			l.add(getCollectionLink(this.collectionImage, iCollectionCategoryId, iwc));
		}
		else {
			Text collText = new Text(this.iwrb.getLocalizedString("collection", "Collection"));
			if (this.showCollectionText) {
				collText = setInformationAttributes(collText);
			}
			l.add(getCollectionLink(collText, iCollectionCategoryId, iwc));
		}
		return l;
	}

	protected PresentationObject getNewsImage(NewsHelper newsHelper, String headline) {
		List files = newsHelper.getContentHelper().getFiles();
		if (files != null && !files.isEmpty()) {
			try {
				// Table imageTable = new Table(1, 2);
				ICFile imagefile = (ICFile) files.get(0);
				int imid = ((Integer) imagefile.getPrimaryKey()).intValue();

				Image newsImage = new Image(imid);
				Link L = new Link(newsImage);
				L.addParameter(ImageWindow.prmImageId, imid);
				if (this.addImageInfo) {
					L.addParameter(ImageWindow.prmInfo, TextSoap.convertSpecialCharacters(headline));
				}
				L.setWindowToOpen(ImageWindow.class);
				return L;
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

}
