
/***************************************************************************************************/
			
function fgseuSearch(sform) {
	if (sform.id == 'osform') {
		makeQuery(sform);
	}
	fgseuQuery(sform, query);
};

function fgseuQuery(sform, fgseuquery) {
	sentQuery = fgseuquery;
	if (sentQuery.length == 0) {
		sentQuery = 'COUNTTYPE:paper';
	}
	// the first replace is necessary because of the way we pass params to fgseu
	sentQuery = sentQuery.replace(/\//g, '%252F'); 
	// the next replaces are necessary for the query string to fedoragsearch
	sentQuery = sentQuery.replace(/\?/g, '%3F');
	sentQuery = sentQuery.replace(/&/g, '%26');
	sentQuery = sentQuery.replace(/\n/g, ' '); 
	
	(document.getElementById('fullQueryConstructionArea')).value = fgseuquery;
    if ($('#fgseuResultSetArea')) $('#fgseuResultSetArea').html('<center>...</center>');
	var url = 'http://localhost:8080/fedoragsearch/rest?operation=gfindObjects&restXslt=fgseuSearchResultToHtml';
	url += '&query='+sentQuery;
	url += '&hitPageStart='+sform.hitPageStart.value;
	url += '&hitPageSize='+sform.hitPageSize.value;
	url += '&sortFields='+sform.sortFields.value;
	
	fgseuResultSetElementId = 'fgseuResult';
	$('#fgseuResultSetArea').load(
			encodeURI(url)+' #'+fgseuResultSetElementId,
			function(response, status, xhr) {
				if (status == 'error') {
					alert('ajaxError: '+xhr.status+' '+xhr.statusText+' \nurl='+url); 
				} else {
					(document.getElementById('urlTextArea')).value = baseRoot+url;
					refineSearch(latestFacetLabel, latestFacet);
				}
			}
	);
};

function refine(indexFieldLabel, indexFieldName) {
	refineField(indexFieldLabel, indexFieldName);
	refineResultSet(indexFieldLabel, indexFieldName);
};

function refineSearch(indexFieldLabel, indexFieldName){	
	var lastIndexRowDivName = indexFieldName+'row';
	var lastIndexRowDiv = document.getElementById(lastIndexRowDivName);
	
	if($(lastIndexRowDiv).is(':visible')){
		var rightArrowName = 'right_'+indexFieldName;
		var downArrowName = 'down_'+indexFieldName;
		var rightArrowDiv = document.getElementById(rightArrowName);
		var downArrowDiv = document.getElementById(downArrowName);
		if(!$(downArrowDiv).is(':visible')){
			$(rightArrowDiv).hide();
			$(downArrowDiv).show();
		}
		refine(indexFieldLabel, indexFieldName);
	}
};

function refineField(indexFieldLabel, indexFieldName) {
	var browseOK = sentQuery.length > 1;
	termsShownField = indexFieldName;
	browseFrom = '!';
	if ( document.getElementById(indexFieldName+'browseFrom') != null ) {
		browseFrom = document.getElementById(indexFieldName+'browseFrom').innerHTML;
	}
	fgseuBrowseTerms(indexFieldLabel, indexFieldName, browseFrom, 12);
};

function fgseuBrowseForm(bform) {
	fgseuBrowseTerms(bform.fieldLabel.value, bform.fieldName.value, bform.startTerm.value, bform.termPageSize.value);
};

String.prototype.endsWith = function(str)
{return (this.match(str+"$")==str)}

function hideChildWith(indexFieldName){
	var termDivName = 'fgseuRightColumnArea1'+indexFieldName;
	var facetDivName = 'fgseuRightColumnArea2'+indexFieldName;
	var termDiv = document.getElementById(termDivName);
	var facetDiv = document.getElementById(facetDivName);
	// Hide TermDiv
	if (termDiv != undefined){
		$(termDiv).hide();
	}
	// Hide FacetDiv
	if (facetDiv != undefined){
		$(facetDiv).hide();
	}
};

function fgseuBrowseTerms(indexFieldLabel, indexFieldName, startTerm, termPageSize) {
	var browseDivName = 'fgseuRightColumnArea1'+indexFieldName;
	var browseDiv = document.getElementById(browseDivName);
    if (browseDiv) browseDiv.innerHTML='<center>...</center>';
	(document.getElementById(browseDivName)).style.display = '';
    var start = startTerm;
    if (start.length == 0 || start.charAt(0) == " ") {
    	start = "!";
    }
    var spacei = start.indexOf(" ");
    if (spacei > -1) {
    	start = start.substring(0, spacei);
    }
    
	var url = 'http://localhost:8080/fedoragsearch/rest';
	url += '?operation=browseIndex&restXslt=fgseuBrowseTermsToHtml';
	url += '&fieldName='+indexFieldName;
	url += '&startTerm='+start;
	url += '&termPageSize='+termPageSize;
	url += '&indexFieldLabel='+indexFieldLabel;
	
	$.get(encodeURI(url),
		function(response, status, xhr) {
			if (status == 'error') {
				alert('ajaxError: '+xhr.status+' '+xhr.statusText+' \nurl='+url); 
			} else {
				document.getElementById(indexFieldName+'browseFrom').innerHTML = startTerm;
				latestFacet = indexFieldName;
				latestFacetLabel = indexFieldLabel;
				$(browseDiv).html(response);
			}
	});
};

function refineResultSet(indexFieldLabel, indexFieldName) {
	fgseuFacetTerms(indexFieldLabel, indexFieldName, 0, 50);
};

function fgseuFacetForm(bform) {
	fgseuFacetTerms(bform.fieldLabel.value, bform.fieldName.value, bform.termOffset.value, 20);
};

function fgseuFacetTerms(indexFieldLabel, indexFieldName, termOffset, termPageSize) {
	if (sentQuery.length < 2) {
//		alert('No search result set exists');
		return;
	}
	var facetDivName = 'fgseuRightColumnArea2'+indexFieldName;
	var facetDiv = document.getElementById(facetDivName);
    if (facetDiv) facetDiv.innerHTML='<center>...</center>';
    facetDiv.style.display = '';
	
	// the first replace is necessary because of the way we pass params to fgseu
	sentQuery = sentQuery.replace(/\//g, '%252F'); 
	// the next replaces are necessary for the query string to fedoragsearch
	sentQuery = sentQuery.replace(/\?/g, '%3F');
	sentQuery = sentQuery.replace(/&/g, '%26');
	sentQuery = sentQuery.replace(/\n/g, ' ');

	var url = 'http://localhost:8080/fedoragsearch/rest?operation=gfindObjects&restXslt=fgseuFacetTermsToHtml';
	url += '&query=(::SOLR::';
	url += 'facet=true';
	url += '%26facet.field='+indexFieldName;
	url += '%26facet.sort=count';
	url += '%26facet.limit='+termPageSize;
	url += '%26facet.offset='+termOffset;
	url += '%26facet.mincount=1';
	url += '%26facet.missing=true';
	url += '%26start=0%26rows=0';
	url += '%26fl='+indexFieldName;
	url += '%26indent=on%26version=2.2%26explainOther=';
	url += '%26q='+sentQuery;
	url += "::SOLR::)";

	$.get(encodeURI(url),
			function(response, status, xhr) {
				facetDiv.style.display = '';
				if (status == 'error') {
					alert('ajaxError: '+xhr.status+' '+xhr.statusText+' \nurl='+url); 
				} else {
					$(facetDiv).html(response); 
				}
		});
	latestFacet = indexFieldName;
	latestFacetLabel = indexFieldLabel;
};

function fgseuShowRightColumnHeader(label) {
	(document.getElementById('fgseuRightColumnTitle')).innerHTML = label;
	(document.getElementById('fgseuRightColumnHeader')).style.display = '';
}
			
/***************************************************************************************************/
			
			function makeQuery (oform) {
				defaultIndexFieldName = 'ALL';
				query = '';
				includeFieldType ('fgseuInputArea', ' AND '); 
			}
			
			function includeFieldType (fdivname, opBetweenFields) {
				inputArea = document.getElementById(fdivname);					// find the div
				inputs = inputArea.getElementsByTagName('input');				// find its inputs
				for ( i in inputs ) {											// one for each field of the type
					input = inputs[i];
					if (input.type == 'text') {
						var fieldquery = includeField(input.name, input);
						if (fieldquery.length > 0) {
							if (query.length > 0) {
								query += opBetweenFields;
							}
							if (input.name == defaultIndexFieldName) {
								fieldquery = '(('+fieldquery+') OR TITLE:('+fieldquery+') OR ABSTRACT:('+fieldquery+'))';
							} else {
								query += input.name+':';
							}
							query += '(\n'+fieldquery+'\n)\n';
						}
					}
				}
			}
			
			function includeField (indexFieldName, ifield) {
				var fieldRow = document.getElementById(indexFieldName+'row');
				if (fieldRow.style.display == 'none') {
					return '';											// only displayed fields
				}
				var opWithinFields = ' AND ';
				var fieldUiUse = document.getElementById(indexFieldName+'uiUse');
				if (fieldUiUse != null) {
					if (fieldUiUse.innerHTML.indexOf('OR') > -1) {
						opWithinFields = ' OR ';
					}
				}
//				clearFacetIfInit(fname);
				var fvalue = ifield.value;								// form field value
				if (fvalue == getInitValue(indexFieldName)) {
					return '';
				}
				var fvaluecleaned = '';									// will be cleaned version of form field value
				var fieldquery = '';									// will be terms with operator in between
				var i = 0;												// start of first term
				var j = fvalue.length;									// end of term
				var ipar = fvalue.indexOf('(');							// first start of parenthesis
				var iquot = fvalue.indexOf('"');						// first start of quote
				while (i<fvalue.length) {
					j = fvalue.indexOf(' ', i);							// first space as end of term
					if (j<0) 
						j = fvalue.length;
					var jpar = findClosingPar(fvalue, ipar);			// matching end parenthesis
					var jquot = fvalue.indexOf('"', iquot+1);			// matching end quote
					if (ipar > -1) {									// if there is a parenthesis
						if (iquot < 0 || ipar < iquot) { 				//   and it starts before a quote
							if (ipar < j) { 							//     and before the end of term
								if (ipar < jpar) {						//       and it ends (else parse error later)
									j = jpar+1;							// then we have a term in parenthesis
								}
							}
						}
					}
					if (iquot > -1) {									// if there is a start quote
						if (ipar < 0 || iquot < ipar) { 				//   and it starts before a parenthesis
							if (iquot < j) { 							//     and before the end of term
								if (iquot < jquot) {					//       and it ends (else parse error later)
									j = jquot+1;							// then we have a term in quotes
								}
							}
						}
					}
					if (j>i) {
						fieldterm = fvalue.slice(i,j);					// this is the term
						if (fieldterm == '""' || fieldterm == '()' || fieldterm == '+' || fieldterm == '-') {
							fieldterm = '';
						}
						if (fieldterm.length > 0) {
							firstchar = fieldterm.charAt(0);
						}
						while (fieldterm.length > 0 && 					// skip some leading characters
								!(firstchar == '(' || firstchar == '"'
									|| firstchar == '+' 					// + means term must be present (Lucene syntax)
									|| firstchar == '-'  					// - means term must NOT be present (Lucene syntax)
									|| ( firstchar >= '0' && firstchar <= '9' )  
									|| ( firstchar >= 'A' && firstchar <= 'Z' )  
									|| ( firstchar >= 'a' && firstchar <= 'z' )  
									|| firstchar == '�' || firstchar == '�' 
									|| firstchar == '�' || firstchar == '�' 
									|| firstchar == '�' || firstchar == '�' 
								 )
								) {
								fieldterm = fieldterm.substring(1);			// skip it
								if (fieldterm.length > 0) {
									firstchar = fieldterm.charAt(0);
								}
						}
						if (fieldterm.length > 0) {
							lastchar = fieldterm.charAt(fieldterm.length-1);
							if (!(lastchar == ')' || lastchar == '"' || lastchar == '*' || lastchar == '?'
										|| ( lastchar >= '0' && lastchar <= '9' )  
										|| ( lastchar >= 'A' && lastchar <= 'Z' )  
										|| ( lastchar >= 'a' && lastchar <= 'z' )  
										|| lastchar == '�' || lastchar == '�' 
										|| lastchar == '�' || lastchar == '�' 
										|| lastchar == '�' || lastchar == '�' 
								)) {
								fieldterm = fieldterm.substring(0, fieldterm.length-1);// skip ending character :
							}
							if (fvaluecleaned.length > 0) {
								fvaluecleaned += ' ';
							}
							fvaluecleaned += fieldterm;							// the field term is now cleaned
							if (fieldquery.length > 0) {
								fieldquery += opWithinFields;					// add operator between terms in field
							}
							fieldquery += fieldterm;							// add term
						}
						i = j;													// next start of term
					} else {													// else a space will be skipped
						i = j+1;												// next start of term
					}
					ipar = fvalue.indexOf('(', i);								// next start of parenthesis
					iquot = fvalue.indexOf('"', i);								// next start of quote
				}
				ifield.value = fvaluecleaned;	// return the cleaned field value to the form field
				return fieldquery;
			}
			
			function findClosingPar(fvalue, ipar) {
				var iparpar = fvalue.indexOf('(', ipar+1);
				var jpar = fvalue.indexOf(')', ipar+1);
				if (ipar > -1 && iparpar > -1 && jpar > -1 && jpar > iparpar) {
					jpar = findClosingPar(fvalue, iparpar);
					jpar = fvalue.indexOf(')', jpar+1);
				}
				return jpar;
			}

/***************************************************************************************************/
			
					function showFacet (fieldLabel, indexFieldName) {
						var fieldRow = document.getElementById(indexFieldName+'row');
						if (fieldRow != null) {
							fieldRow.style.display = '';
						} else { 
							alert('field row not found: '+indexFieldName);
						}
						var fieldInput = document.getElementById(indexFieldName);
						if (fieldInput != null) {
							if (fieldInput.value.length == 0) {
								fieldInput.value = getInitValue(indexFieldName);
							}
							fieldInput.select();
						} else { 
							alert('field input not found: '+indexFieldName);
						}
						
						var termAreaDivName = 'fgseuRightColumnArea1'+indexFieldName;
						var termAreaDiv = document.getElementById(termAreaDivName);
						var facetAreaDivName = 'fgseuRightColumnArea1'+indexFieldName;
						var facetAreaDiv = document.getElementById(termAreaDivName);
						var rightArrowName = 'right_'+indexFieldName;
						var downArrowName = 'down_'+indexFieldName;
						var rightArrowDiv = document.getElementById(rightArrowName);
						var downArrowDiv = document.getElementById(downArrowName);
						
						$(rightArrowDiv).toggle();
						$(downArrowDiv).toggle();
						if($(facetAreaDiv).is(':visible') || $(termAreaDiv).is(':visible')){
							hideChildWith(indexFieldName);
						} else {
							refine(fieldLabel, indexFieldName);
						}
					}
			
					function clearFacet (indexFieldName) {
						var fieldInput = document.getElementById(indexFieldName);
						if (fieldInput != null) {
							fieldInput.value = getInitValue(indexFieldName);
							fieldInput.select();
						} else { 
							alert('field input not found: '+indexFieldName);
						}
					}
			
					function hideFacet (indexFieldName) {
						defaultIndexFieldName = 'ALL';
						var fieldRow = document.getElementById(indexFieldName+'row');
						if (fieldRow != null) {
							if (indexFieldName != defaultIndexFieldName) {
								fieldRow.style.display = 'none';
							}
						} else { 
							alert('field row not found: '+indexFieldName);
						}
						clearFacet(indexFieldName);
						hideChildWith(indexFieldName);
						
						var rightArrowName = 'right_'+indexFieldName;
						var downArrowName = 'down_'+indexFieldName;
						var rightArrowDiv = document.getElementById(rightArrowName);
						var downArrowDiv = document.getElementById(downArrowName);
						if($(downArrowDiv).is(':visible')){
							$(rightArrowDiv).toggle();
							$(downArrowDiv).toggle();
						}
					}

/*** Handling of field initValue ****************************************/

					function getInitValue (indexFieldName) {
						var initValueDiv = document.getElementById(indexFieldName+'initValue');
						var initvalue = '';
						if (initValueDiv != null) {
							initvalue = initValueDiv.innerHTML;
						} else { 
							alert('initValue not found: '+indexFieldName);
						}
						return initvalue;
					}
					
					function fieldOnClick (indexFieldName) {
						clearFacetIfInit (indexFieldName);
					}
					
					function fieldOnFocus (indexFieldName) {
//						clearFacetIfInit (indexFieldName);
					}
					
					function fieldOnBlur (indexFieldName) {
//						clearFacetIfInit (indexFieldName);
					}
					
					function clearFacetIfInit (indexFieldName) {
						var fieldInput = document.getElementById(indexFieldName);
						if (fieldInput != null) {
							if (fieldInput.value.length > 0 && fieldInput.value == getInitValue(indexFieldName)) {
								fieldInput.value = '';
							}
						} else { 
							alert('field input not found: '+indexFieldName);
						}
					}
			
/*** Move term from browseIndex to field as search term ****************************************/
/*** Remove term if already set as search term ****************************************/
			
					function searchFor (term, indexFieldName) {
						var fieldRow = document.getElementById(indexFieldName+'row');
						if (fieldRow != null) {
							fieldRow.style.display = '';
							var fieldInput = document.getElementById(indexFieldName);
							if (fieldInput != null) {
								clearFacetIfInit(indexFieldName);
								var fieldValue = fieldInput.value;
								var termlocal = term;
								var isp = term.indexOf(' ');
								if (isp > 0) {
									termlocal = '"'+term+'"';
								}
								var i = fieldValue.indexOf(termlocal);
								if (i<0) {
									fieldInput.value += ' '+termlocal;
								} else {
//									alert(fieldValue.substring(0,i) + fieldValue.substring(i+term.length));
									fieldInput.value = fieldValue.substring(0,i).concat(fieldValue.substring(i+termlocal.length));
								}
							} else { 
								alert('field input not found: '+indexFieldName);
							}
						}
					}
			
/***************************************************************************************************/
						
					function getTimeString () {
						var date = new Date();
						var timeString = date.getHours()+':'+date.getMinutes()+':'+date.getSeconds();
						return timeString;
					}

					/***************************************************************************************************/

					function fgseuShowFacets(sform) {
						(document.getElementById('showfacetsbutton')).style.display = 'none';
						(document.getElementById('hidefacetsbutton')).style.display = '';
						if(document.getElementById('openfullquerybutton') != null){
							(document.getElementById('openfullquerybutton')).style.display = '';
						}
						(document.getElementById('showurlbutton')).style.display = '';
						(document.getElementById('fgseuBasicSearchTipsLink')).style.display = 'none';
						(document.getElementById('fgseuAdvancedSearchTipsLink')).style.display = '';
						refine(latestFacetLabel, latestFacet); 
					};

					function fgseuShowMoreFacets(sform) {
						(document.getElementById('showmorefacetsbutton')).style.display = 'none';
						(document.getElementById('hidemorefacetsbutton')).style.display = '';
					};
								
					function fgseuHideMoreFacets() {
						(document.getElementById('showmorefacetsbutton')).style.display = '';
						(document.getElementById('hidemorefacetsbutton')).style.display = 'none';
					};

					/***************************************************************************************************/
								
					function fgseuOpenFullQuery(sform) {
						(document.getElementById('openfullquerybutton')).style.display = 'none';
						(document.getElementById('closefullquerybutton')).style.display = '';
						(document.getElementById('fullQueryConstruction')).style.display = '';
						if ((document.getElementById('fullQueryConstructionArea')).value.length < 2) {
							fgseuRefreshFullQuery(sform);
						}
					};
								
					function fgseuCloseFullQuery() {
						(document.getElementById('fullQueryConstruction')).style.display = 'none';
						(document.getElementById('closefullquerybutton')).style.display = 'none';
						(document.getElementById('openfullquerybutton')).style.display = '';
					};
								
					function fgseuRefreshFullQuery(sform) {
						makeQuery(sform);
						(document.getElementById('fullQueryConstructionArea')).value = query;
					};

					/***************************************************************************************************/
								
					function showSearchUrl() {
						(document.getElementById('showurlbutton')).style.display = 'none';
						(document.getElementById('hideurlbutton')).style.display = '';
						(document.getElementById('copySearchUrl')).style.display = '';
						(document.getElementById('urlTextArea')).focus();
						(document.getElementById('urlTextArea')).select();
					};
								
					function hideSearchUrl() {
						(document.getElementById('copySearchUrl')).style.display = 'none';
						(document.getElementById('hideurlbutton')).style.display = 'none';
						(document.getElementById('showurlbutton')).style.display = '';
					};

					/***************************************************************************************************/
								
					function showLessMetadata(pid) {
						(document.getElementById('fgseuResultSetCellMore'+pid)).style.display = 'none';
						(document.getElementById('fgseuShowMoreMetadata'+pid)).style.display = 'none';
						(document.getElementById('fgseuResultSetCellLess'+pid)).style.display = '';
						(document.getElementById('fgseuShowLessMetadata'+pid)).style.display = '';
					};
								
					function showMoreMetadata(pid) {
						(document.getElementById('fgseuResultSetCellLess'+pid)).style.display = 'none';
						(document.getElementById('fgseuShowLessMetadata'+pid)).style.display = 'none';
						(document.getElementById('fgseuResultSetCellMore'+pid)).style.display = '';
						(document.getElementById('fgseuShowMoreMetadata'+pid)).style.display = '';
						(document.getElementById('fgseuResultSetCellRef'+pid)).focus();
						(document.getElementById('fgseuResultSetCellRef'+pid)).select();
					};

					function showMetadataPage(pid) {
						alert('showMetadataPage is not implemented');
					};
								
/***************************************************************************************************/
