(function() {

var addPanel,
  list,
  addForm,
  foundation;


var appController = window.appController = {
  _currentItem: null,

  $ready : function() {
    addPanel = hui.ui.get('addPanel');
    list = hui.ui.get('list');
    addForm = hui.ui.get('addForm');
    foundation = hui.ui.get('foundation');
    //this.$select$list({id:4682437,kind:'Question'})
    hui.listen(window,'popstate',function(e) {
      hui.log(e);
      this.show(e.state, {push: false});
    }.bind(this));
    this._applyInitialState();

    new Editable(hui.find('#questionTitle'), this._updateQuestion.bind(this));
    new Editable(hui.find('#statementTitle'), this._updateStatement.bind(this));
    new Editable(hui.find('#hypothesisTitle'), this._updateHypothesis.bind(this));
  },
  _applyInitialState : function() {
    this._updateUI();
    var state = history.state
    if (!state) {
      state = {
        id: hui.location.getInt('id'),
        type: hui.location.getParameter('type')
      }
    }
    if (state.type && state.id) {
      window.setTimeout(function() {
        this.show(state);
      }.bind(this))
    }
  },


  // UI
  
  _updateUI : function() {
    var favoriteCheckbox = hui.ui.get('favorite');
    var inboxCheckbox = hui.ui.get('inbox');
    var viewMode = hui.ui.get('viewMode');
    var enabled = !!this._currentItem;
    hui.ui.get('deleteObject').setEnabled(enabled);
    hui.ui.get('addWord').setEnabled(enabled);
    favoriteCheckbox.setEnabled(enabled);
    inboxCheckbox.setEnabled(enabled);
    viewMode.setVisible(this._currentItem && this._currentItem.type == 'InternetAddress');
    if (this._currentItem) {
      favoriteCheckbox.setValue(this._currentItem.favorite);
      inboxCheckbox.setValue(this._currentItem.inbox);
    } else {
      favoriteCheckbox.setValue(false);
      inboxCheckbox.setValue(false);
    }
    list.updateSelection();
  },
  $render$wordSelection : function(item) {
    return hui.build('span', {'class': 'selection selection-word', text: item.text});
  },
  $render$tagSelection : function(item) {
    var node = hui.build('span.selection.selection-tag', {text: item.text});
    var more = hui.build('span.hui_symbol.hui_symbol-more.selection_more');
    hui.on(more, 'click', function(e) {
      hui.stop(e);
      this._editTag({node:more,tag:item})
    }.bind(this))
    node.appendChild(more);
    return node;
  },
  _editedTag : null,
  _editTag : function(props) {
    var tag = props.tag;
    this._editedTag = tag;
    hui.ui.get('editTagPanel').show({target: props.node})
    hui.ui.get('editTagForm').setValues({text: tag.text});
    hui.ui.get('editTagForm').focus();
  },
  $submit$editTagForm : function(form) {
    var text = form.getValues().text;
    if (hui.isBlank(text)) {
      return;
    }
    var panel = hui.ui.get('editTagPanel');
    panel.setBusy(true);
    hui.ui.request({
      url: '/app/tag',
      method: 'PUT',
      parameters: {
        id: this._editedTag.id,
        text: text
      },
      $success : function() {
        hui.ui.get('tagsSource').refresh();
        this._reloadCurrentPerspective();
      }.bind(this),
      $finally : function() {
        panel.setBusy(false);
        panel.hide();
      }
    });
  },
  tagSelection : function() {
    return hui.ui.get("tagSelection");
  },
  $click$deleteTag : function() {
    var currentTagSelection = this.tagSelection().getValue();
    if (currentTagSelection) {
      if (currentTagSelection.id = this._editedTag.id) {
        this.tagSelection().reset();
      }
    }
    var panel = hui.ui.get('editTagPanel');
    panel.setBusy(true);
    hui.ui.request({
      url: '/app/tag',
      method: 'DELETE',
      parameters: {
        id: this._editedTag.id
      },
      $success : function() {
        hui.ui.get('tagsSource').refresh();
        this._reloadCurrentPerspective();
      }.bind(this),
      $finally : function() {
        panel.setBusy(false);
        panel.hide();
      }
    });
  },
  // Adding

  $click$addButton : function(button) {
    addPanel.show({target: button.getElement()})
    addForm.focus();
  },

  $click$listAddButton : function(button) {
    //addPanel.show({target: hui.ui.get('addButton').getElement()})
    addPanel.show({target: button.getElement()})
    addForm.focus();
  },

  $submit$addForm : function(form) {
    var text = form.getValues().text;
    addPanel.setBusy(true);
    this._createFromText(text).then((obj) => {
      form.reset();
      addPanel.setBusy(false);
      this._viewNewItem(obj)
      addPanel.hide();
    },(t) => {
      this._showRequestError(t);
      addPanel.setBusy(false);
    });
  },
  _createFromText : function(text) {
    if (/\?$/.test(text)) {
      return this._createQuestion(text);
    } else if (/\!$/.test(text)) {
      return this._createHypothesis(text);
    } else if (/^http/.test(text)) {
      return this._createAddress(text);
    } else {
      return this._createStatement(text);
    }
  },

  _showRequestError : function(request) {
    var info = hui.string.fromJSON(request.responseText)
    if (info) {
      hui.ui.msg.fail({text: info.message})
    } else {
      hui.ui.msg.fail({text: 'An unexpected error occurred'})
    }
  },


  // Deleting

  $click$deleteObject : function(button) {
    if (!this._currentItem) {
      return;
    }
    this._whileBusy((unBusy) => {
      hui.ui.request({
        url: '/app/delete',
        parameters: {
          id: this._currentItem.id,
          type: this._currentItem.type
        },
        $success : function() {
          unBusy();
          this._changeItem(null)
          history.back()
          //history.replaceState(null, document.title, document.location.pathname);
          // TODO go back + pop history
          list.refresh();
          hui.ui.get('wordsSource').refresh();
          this._reset();
        }.bind(this),
        $failure : function() {
          // TODO: Handle erroe
          unBusy();
        }
      });
    });
    
  },

  $click$goBack : function(button) {
    history.back();
  },
  $click$goForward : function(button) {
    history.forward();
  },
  $valueChanged$favorite : function(value) {
    hui.ui.request({
      url: '/app/favorite',
      parameters: {
        id: this._currentItem.id,
        type: this._currentItem.type,
        favorite: value
      },
      $success: function() {
        hui.ui.get('list').refresh();
      }
    })
  },
  $valueChanged$inbox : function(value) {
    hui.ui.request({
      url: '/app/inbox',
      parameters: {
        id: this._currentItem.id,
        type: this._currentItem.type,
        inbox: value
      },
      $success: function() {
        hui.ui.get('list').refresh();
      }
    })
  },

  // Tags
  
  $click$addTag: function(button) {
    hui.ui.get('tagPanel').show({target: button.getElement()});
    hui.ui.get('addTagForm').focus();
  },
  $submit$addTagForm : function(form) {
    var text = form.getValues().text;
    if (hui.isBlank(text)) {
      return;
    }
    this.createTagOnCurrentItem(text);
    form.reset();
  },
  
  createTagOnCurrentItem : function(text) {
    hui.ui.request({
      url: '/app/tag',
      method: 'POST',
      parameters: {
        text: text,
        type: this._currentItem.type,
        id: this._currentItem.id
      },
      $object: function(obj) {
        this._updatePerspective(obj);
        hui.ui.get('tagsSource').refresh();
        list.refresh();
      }.bind(this)
    })
  },
  _renderTag : function(obj) {
    var item = hui.build('span.relations_item.relations_item-tag', { text: obj.label });
    var remove = hui.build('span.relations_remove', { parent: item });
    hui.on(remove, 'click', function(e) {

      e.preventDefault();
      hui.ui.confirmOverlay({
        text: 'Really remove?', element: remove,
        $ok: function() { 
          this._removeTag({id: obj.value});
        }.bind(this)
      });
      
    }.bind(this));
    return item;    
  },
  _removeTag : function(tag) {
    hui.ui.request({
      url: '/app/tag/remove',
      method: 'DELETE',
      parameters: {
        tagId: tag.id,
        type: this._currentItem.type,
        id: this._currentItem.id
      },
      $object: function(obj) {
        this._updatePerspective(obj);
        hui.ui.get('tagsSource').refresh();
        list.refresh();
      }.bind(this)
    })
  },

  // Words
  
  $click$addWord : function() {
    oo.WordFinder.get().show();
  },
  $found$wordFinder : function(e) {
    this._addWord(e);
  },
  _addWord : function(tag) {
    hui.ui.request({
      url: '/app/word',
      method: 'POST',
      parameters: {
        wordId: tag.id,
        type: this._currentItem.type,
        id: this._currentItem.id
      },
      $object: function(obj) {
        this._updatePerspective(obj);
        hui.ui.get('wordsSource').refresh();
        list.refresh();
      }.bind(this)
    })
  },
  _removeWord : function(tag) {
    hui.ui.request({
      url: '/app/word',
      method: 'DELETE',
      parameters: {
        wordId: tag.id,
        type: this._currentItem.type,
        id: this._currentItem.id
      },
      $object: function(obj) {
        this._updatePerspective(obj);
        hui.ui.get('wordsSource').refresh();
        list.refresh();
      }.bind(this)
    })
  },
  _renderWord : function(obj) {
    var item = hui.build('span.relations_item.relations_item-word', { text: obj.label });
    var remove = hui.build('span.relations_remove', { parent: item });
    hui.on(remove, 'click', function(e) {

      e.preventDefault();
      hui.ui.confirmOverlay({
        text: 'Really remove?', element: remove,
        $ok: function() { 
          this._removeWord({id: obj.value});
        }.bind(this)
      });
      
    }.bind(this));
    return item;    
  },


  // List

  $select$list : function(item) {
    if (!item) { return; }
    if (item.data.type === 'InternetAddress') {
      this.show({
        id: item.data.id,
        title: item.data.text,
        type: item.data.type,
        url: item.data.url
      });
    } else {
      this.show(item.data);
    }
  },
  $isSelected$list : function(obj) {
    return this._currentItem && this._currentItem.id === obj.id;
  },
  $empty$list : function(obj) {
    var query = hui.ui.get('search').getValue();
    var subset = hui.ui.get('subsetSelection').getValue();
    var type = hui.ui.get('typeSelection').getValue();
    var word = hui.ui.get('wordSelection').getValue();
    var tag = hui.ui.get('tagSelection').getValue();
    var cls = 'no_content';
    if (query) {
      cls = 'empty_search';
    } else if (word.value || tag.value) {
      cls = 'empty_selection';
    } else if (subset.value === 'everything') {
      if (type.value === 'InternetAddress') {
        cls = 'empty_internetaddress';
      } else if (type.value === 'Statement') {
        cls = 'empty_statement';
      } else if (type.value === 'Question') {
        cls = 'empty_question';
      } else if (type.value === 'Hypothesis') {
        cls = 'empty_hypothesis';
      }
    } else if (subset.value === 'favorite') {
      cls = 'no_favorites';
    } else if (subset.value === 'inbox') {
      cls = 'empty_inbox';
    } else if (subset.value === 'archive') {
      cls = 'empty_archive';
    }
    var x = hui.findAll('.list_empty_text')
    for (var i = 0; i < x.length; i++) {
      x[i].style.display = x[i].getAttribute('data') === cls ? 'block' : 'none';
    }
  },

  $render$list : function(obj) {
    var item = hui.build('div.list_item');
    hui.cls.add(item, 'list_item-' + obj.type.toLowerCase());
    if (obj.favorite) {
      hui.build('div.list_item_favorite', {parent: item});
    }
    hui.build('div.list_item_text', {text: obj.text,parent: item});
    /*
    if (obj.inbox) {
      hui.build('div.list_item_inbox', {parent: item});
    }*/
    return item;
  },


  show : function(item, options) {
    if (!item) {
      this._changeItem(null);
      return;
    }
    this._reset();

    var func = this['_on'+item.type].bind(this);
    func(item);
    this._whileBusy((end) => {
      this._loadPerspective(item).then((obj) => {
        func(obj);
        end();
      }, (error) => {
        this._loadFailed();
        end();
      });
    });
    if (options === undefined || options.push !== false) {
      this._pushState(item);
    }
  },
  _reloadCurrentPerspective : function() {
    var item = this._currentItem;
    if (item) {
      var func = this._updatePerspective.bind(this);
      this._whileBusy((end) => {
        this._loadPerspective(item).then((obj) => {
          func(obj);
          end();
        }, (error) => {
          this._loadFailed();
          end();
        });
      });
    }
  },
  _updatePerspective : function(item) {
    this['_on'+item.type](item);
  },
  _loadFailed : function() {
    this._showError({
      title: 'The object could not be found',
      text: 'It is probably because it has been deleted'
    });
  },
  _showError : function(params) {
    hui.dom.setText(hui.find('.warning_title'), params.title);
    hui.dom.setText(hui.find('.warning_text'), params.text);
    this._changeFragment('error');
  },
  _pushState : function(item) {
    if (history.state && history.state.id == item.id) {
      history.replaceState(item,'TODO',document.location.pathname + '?type='+item.type+'&id='+item.id);
    } else {
      history.pushState(item,'TODO',document.location.pathname + '?type='+item.type+'&id='+item.id);
    }
  },
  _reset : function() {
    documentController.reset();
    hui.ui.get('questionFinder').hide();
  },
  _changeItem : function(item) {
    this._changeFragment(item ? item.type.toLowerCase() : 'intro');
    this._currentItem = item;
    this._updateUI();
    if (item) {
      this._pushState(item);
    }
  },
  _changeFragment : function(name) {
    ['intro','internetaddress','question','statement','hypothesis','error'].forEach(function(id) {
      hui.ui.get(id).setVisible(name == id);
    });
  },
  _loadPerspective : function(perspective) {
    return new Promise(function(resolve, reject) {
      hui.ui.request({
        url: '/app/' + perspective.type.toLowerCase(),
        parameters: {
          id: perspective.id
        },
        $object : resolve,
        $failure : reject
      })
    });
  },
  _viewNewItem : function(item) {
    list.refresh();
    this._pushState(item);
    if (item.type == 'InternetAddress') {
      this._onInternetAddress(item);
    } else if (item.type == 'Statement') {
      this._onStatement(item);
    } else if (item.type == 'Question') {
      this._onQuestion(item);
    } else if (item.type == 'Hypothesis') {
      this._onHypothesis(item);
    }
  },

  _render_relation : function(item, context) {
    var text = item.title || item.text;
    var info = item.url ? item.url.match(/\/\/(www\.)?([^\/]+)/)[2] : undefined;
    var node = hui.build('div.perspective_relation');
    node.appendChild(hui.build('div.perspective_relation_title',{text: text}));
    if (info) {
      node.appendChild(hui.build('div.perspective_relation_info',{text: info}));
    }
    if (context.$remove) {
      var rm = hui.build('div.perspective_relation_remove');
      hui.on(rm,'click',function(e) {
        hui.stop(e);
        hui.ui.confirmOverlay({
          text: 'Really remove?', element: rm,
          $ok: function() { context.$remove(item) }
        });
      });
      node.appendChild(rm);      
    }
    return node;
  },

  // Question

  _createQuestion : function(text) {
    return new Promise(function(resolve, reject) {
      hui.ui.request({
        url: '/app/question/create',
        parameters: { text: text },
        $object : resolve, $failure : reject
      });
    });
  },
  _updateQuestion : function(text) {
    hui.ui.request({
      url: '/app/question/update',
      parameters: {
        id: this._currentItem.id,
        text: text
      },
      $object : function(obj) {
        this._onQuestion(obj);
        list.refresh();
      }.bind(this)
    });
  },
  _onQuestion : function(data) {
    this._changeItem(data);
    hui.dom.setText(hui.find('#questionTitleText'), data.text);
    hui.ui.get('questionAnswers').setData(data.answers);
    hui.ui.get('questionWords').setData(data.words);
    hui.ui.get('questionTags').setData(data.tags);
  },
  $render$questionAnswers : function(statement) {
    return this._render_relation(statement, {$remove: this._removeAnswerFromQuestion.bind(this)});
  },
  $select$questionAnswers : function(e) {
    this.show(e.data);
  },
  $click$addAnswerToQuestion : function() {
    this.statementFinderHandler = this._addStatementToQuestion.bind(this);
    hui.ui.get('statementFinder').show();
  },
  _addStatementToQuestion : function(statement) {
    hui.ui.request({
      url: '/app/question/add/statement',
      parameters: {
        statementId: statement.id,
        questionId: this._currentItem.id
      },
      $object : this._onQuestion.bind(this)
    })
  },
  $select$statementFinder : function(statement) {
    hui.ui.get('statementFinder').hide();
    this.statementFinderHandler(statement);
  },
  _removeAnswerFromQuestion : function(statement) {
    hui.ui.request({ 
      url: '/app/question/remove/statement',
      parameters: {
        statementId: statement.id,
        questionId: this._currentItem.id
      },
      $object : this._onQuestion.bind(this)
    });
  },
  $render$questionWords : function(obj) {
    return this._renderWord(obj);
  },
  $render$questionTags : function(obj) {
    return this._renderTag(obj);
  },

  // Statement

  _createStatement : function(text) {
    return new Promise(function(resolve, reject) {
      hui.ui.request({
        url: '/app/statement/create',
        parameters: { text: text },
        $object : resolve,
        $failure: reject
      })
    })
  },
  _updateStatement : function(text) {
    hui.ui.request({
      url: '/app/statement/update',
      parameters: {
        id: this._currentItem.id,
        text: text
      },
      $object : function(obj) {
        this._onStatement(obj);
        list.refresh();
      }.bind(this)
    });
  },
  _onStatement : function(data) {
    this._changeItem(data);
    hui.dom.setText(hui.find('#statementTitleText'), data.text);
    hui.ui.get('statementQuestions').setData(data.questions);
    hui.ui.get('statementAddresses').setData(data.addresses);
    hui.ui.get('statementWords').setData(data.words);
    hui.ui.get('statementTags').setData(data.tags);
    if (data.questionSuggestions && data.questionSuggestions.suggestions && data.questionSuggestions.suggestions.length) {
      hui.ui.get('statementSuggestions').setData(data.questionSuggestions.suggestions);
      hui.ui.get('statementSuggestionsFragment').show();
    } else {
      hui.ui.get('statementSuggestionsFragment').hide();
    }
  },
  $render$statementQuestions : function(obj) {
    return this._render_relation(obj, {$remove: this._removeQuestionFromStatement.bind(this)});
  },
  $select$statementQuestions : function(e) {
    this.show(e.data);
  },

  $render$statementAddresses : function(obj) {
    return this._render_relation(obj, {});
  },
  $select$statementAddresses : function(e) {
    this.show(e.data);
  },

  $render$statementSuggestions : function(obj) {
    return hui.build('div.perspective_relation', {children:[
      hui.build('div.perspective_relation_title',{text: obj.description})
    ]});
  },
  $select$statementSuggestions : function(e) {
    this._addQuestionToStatement(e.data.entity.id)
  },


  $click$addQuestionToStatement : function() {
    hui.ui.get('questionFinder').show();
  },
  $select$questionFinder : function(obj) {
    hui.ui.get('questionFinder').hide();
    this._addQuestionToStatement(obj.id);
  },
  _addQuestionToStatement : function(id) {
    hui.ui.request({
      url: '/app/statement/add/question',
      parameters: {
        statementId: this._currentItem.id,
        questionId: id
      },
      $object : this._onStatement.bind(this)
    })
  },
  _removeQuestionFromStatement : function(question) {
    hui.ui.request({ 
      url: '/app/statement/remove/question',
      parameters: {
        statementId: this._currentItem.id,
        questionId: question.id
      },
      $object : this._onStatement.bind(this)
    });
  },
  $render$statementWords : function(obj) {
    return this._renderWord(obj);
  },
  $render$statementTags : function(obj) {
    return this._renderTag(obj);
  },

  // Hypothesis

  _createHypothesis : function(text) {
    return new Promise(function(resolve, reject) {
      hui.ui.request({
        url: '/app/hypothesis/create',
        parameters: { text: text },
        $object : resolve, $failure : reject
      });
    });
  },
  _updateHypothesis : function(text) {
    this._whileBusy((end) => {
      hui.ui.request({
        url: '/app/hypothesis/update',
        parameters: {
          id: this._currentItem.id,
          text: text
        },
        $object : function(obj) {
          this._onHypothesis(obj);
          list.refresh();
        }.bind(this),
        $finally : end
      });
    });
  },
  _onHypothesis : function(data) {
    this._changeItem(data);
    hui.dom.setText(hui.find('#hypothesisTitleText'), data.text);
    hui.ui.get('hypothesisSupporting').setData(data.supports);
    hui.ui.get('hypothesisContradicting').setData(data.contradicts);
    hui.ui.get('hypothesisWords').setData(data.words);
    hui.ui.get('hypothesisTags').setData(data.tags);
  },
  $render$hypothesisSupporting : function(obj) {
    return this._render_relation(obj, {$remove: function(rel) {
      this._removeStatementFromHypothesis(rel, 'supports');
    }.bind(this)});
  },
  $render$hypothesisContradicting : function(obj) {
    return this._render_relation(obj, {$remove: function(rel) {
      this._removeStatementFromHypothesis(rel, 'contradicts');
    }.bind(this)});
  },
  $select$hypothesisContradicting : function(e) {
    this.show(e.data);
  },
  $select$hypothesisSupporting : function(e) {
    this.show(e.data);
  },
  $click$addSupportToHypothesis : function() {
    this.statementFinderHandler = function(obj) {
      this._addStatementToHypothesis(obj, 'supports');
    }.bind(this)
    hui.ui.get('statementFinder').show();
  },
  $click$addContradictionToHypothesis : function() {
    this.statementFinderHandler = function(obj) {
      this._addStatementToHypothesis(obj, 'contradicts');
    }.bind(this)
    hui.ui.get('statementFinder').show();
  },
  _removeStatementFromHypothesis : function(statement, relation) {
    this._whileBusy((end) => {
      hui.ui.request({ 
        url: '/app/hypothesis/remove/statement',
        parameters: {
          hypothesisId: this._currentItem.id,
          statementId: statement.id,
          relation: relation
        },
        $object: this._onHypothesis.bind(this),
        $finally: end
      });
    });
  },
  _addStatementToHypothesis : function(statement, relation) {
    this._whileBusy((end) => {
      hui.ui.request({ 
        url: '/app/hypothesis/add/statement',
        parameters: {
          hypothesisId: this._currentItem.id,
          statementId: statement.id,
          relation: relation
        },
        $object: this._onHypothesis.bind(this),
        $finally: end
      });
    });
  },
  $render$hypothesisWords : function(obj) {
    return this._renderWord(obj);
  },
  $render$hypothesisTags : function(obj) {
    return this._renderTag(obj);
  },

  _whileBusy : function(operation) {
    foundation.setBusyMain(true);
    operation(function() {
      foundation.setBusyMain(false);
    })
  },

  // Address

  _loadAddress : function(address) {
    console.log(address);
    foundation.setBusyMain(true);
    hui.ui.request({
      url: '/app/internetaddress',
      parameters: {
        id: address.id
      },
      $object : function(obj) {
        this._onInternetAddress(obj);
        foundation.setBusyMain(false);
      }.bind(this)
    })
  },
  _createAddress : function(url) {
    return new Promise(function(resolve, reject) {
      hui.ui.request({
        url: '/app/internetaddress/create',
        parameters: { url: url },
        $object : resolve, $failure : reject
      })
    });
  },
  _onInternetAddress : function(data) {
    this._changeItem(data);
    var html = data.formatted;
    if (!html) {
      html = '<h1>'+hui.string.escape(data.title || 'Loading...')+'<h1>';
    }
    if (data.words) {
      var prefix = ['<div class="tags">'];
      
      prefix.push('</div>');
    }
    this._changeAddressBody(html);
    var head = hui.find('.js-internetaddress-head');
    head.innerHTML = '';
    if (data.url) {
      hui.build('a', {href:data.url, text: data.url, target: '_blank', parent: head});
    }
    hui.ui.get('internetaddressWords').setData(data.words);
    hui.ui.get('internetaddressTags').setData(data.tags);
    hui.find('.js-internetaddress-text').innerText = data.text || '';
    documentController.reset();
  },
  $valueChanged$viewMode : function(value) {
    hui.find('.js-internetaddress-formatted').style.display = (value == 'formatted' ? '' : 'none')
    hui.find('.js-internetaddress-text').style.display = (value == 'text' ? '' : 'none')
    documentController.reset();
  },
  _changeAddressBody : function(html) {
    hui.find('.js-internetaddress-formatted').innerHTML = html;
  },
  createStatementOnAddress : function(text) {
    this._whileBusy((unBusy) => {
      hui.ui.request({
        url: '/app/internetaddress/create/statement',
        parameters: { id: this._currentItem.id, text: text },
        $object : function(obj) {
          this._onInternetAddress(obj);
          list.refresh();
        }.bind(this),
        $finally : unBusy
      });      
    });
  },
  $render$internetaddressWords : function(obj) {
    return this._renderWord(obj);
  },
  $render$internetaddressTags : function(obj) {
    return this._renderTag(obj);
  }
}

hui.ui.listen(appController);

class Editable {

  constructor(node, callback) {
    this.node = node;
    this.callback = callback;
    this.subject = this.node.firstElementChild;
    this.active = false;
    this.attach();
  }

  attach() {
    this.node.addEventListener('click', this.activate.bind(this));
  }

  activate() {
    if (!this.input) {
      this.input = document.createElement('textarea');
      this.input.className = this.subject.className;
      this.node.appendChild(this.input)
      this.input.addEventListener('blur', this.onBlur.bind(this));
      this.input.addEventListener('input', this.onChange.bind(this));
      this.input.addEventListener('keydown', this.onKey.bind(this));
    }
    if (this.active) return;
    //console.log('activate')
    this.active = true;
    this.canceled = false;
    this.node.classList.toggle('is-active');
    this.orignalValue = this.subject.innerText;
    this.input.value = this.orignalValue;
    this.input.focus();
    this.input.select();
  }

  deactivate() {
    //console.log('deactivate')
    this.node.classList.remove('is-active');
    this.active = false;
  }

  cancel() {
    //console.log('cancel')
    this.canceled = true;
    this.input.blur();
  }

  onBlur() {
    console.log('blur')
    this.deactivate();
    if (this.canceled || this.input.value.match(/[\S]+/) === null) {
      this.update(this.orignalValue);
    }
    else if (this.input.value.trim() !== this.orignalValue.trim()) {
      this.callback(this.input.value);
    } else {
      this.update(this.orignalValue);
    }
  }

  onKey(e) {
    if (e.keyCode == 27) {
      this.cancel();
    }
    else if (e.keyCode == 13 && !(e.altKey || e.shiftKey)) {
      e.preventDefault();
      this.input.blur();
    }
  }

  onChange(e) {
    this.update(this.input.value);
  }

  escape(html){
    var p = document.createElement('p');
    p.appendChild(document.createTextNode(html));
    return p.innerHTML;
  }

  update(text) {
    var html = this.escape(text);
    if (html.endsWith("\n") || html.match(/[\w]+/) === null) {
      html+="&nbsp;"
    }
    this.subject.innerHTML = html.replace(/\n/g,"<br/>")    
  }
}

})();