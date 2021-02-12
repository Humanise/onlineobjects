(function() {

var addPanel,
  list,
  addForm;


var appController = window.appController = {
  _currentItem: null,

  $ready : function() {
    addPanel = hui.ui.get('addPanel');
    list = hui.ui.get('list');
    addForm = hui.ui.get('addForm');
    //this.$select$list({id:4682437,kind:'Question'})
    hui.listen(window,'popstate',function(e) {
      hui.log(e);
      this.show(e.state, {push: false});
    }.bind(this));
    this._applyInitialState();
  },
  _applyInitialState : function() {
    this._updateUI();
    var state = window.state
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
  },

  // Adding

  $click$addButton : function(button) {
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
    hui.ui.request({
      url: '/app/delete',
      parameters: {
        id: this._currentItem.id,
        type: this._currentItem.type
      },
      $success : function() {
        this._changeItem(null)
        history.back()
        //history.replaceState(null, document.title, document.location.pathname);
        // TODO go back + pop history
        list.refresh();
        this._reset();
      }.bind(this)
    })
    
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


  $select$list : function(item) {
    if (!item) { return; }
    this.show({id:item.id, type:item.kind});
  },
  show : function(item, options) {
    if (!item) {
      this._changeItem(null);
      return;
    }
    this._reset();
    if (item.type == 'InternetAddress') {
      this._loadAddress(item);
    } else if (item.type == 'Question') {
      this._loadPerspective(item, this._onQuestion.bind(this));
    } else if (item.type == 'Statement') {
      this._loadPerspective(item, this._onStatement.bind(this));
    } else if (item.type == 'Hypothesis') {
      this._loadPerspective(item, this._onHypothesis.bind(this));
    }
    if (options === undefined || options.push !== false) {
      this._pushState(item);
    }
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
    this._changeFragment(item ? item.type.toLowerCase() : null);
    this._currentItem = item;
    this._updateUI();
  },
  _changeFragment : function(name) {
    ['internetaddress','question','statement','hypothesis','error'].forEach(function(id) {
      hui.ui.get(id).setVisible(name == id);
    });
  },
  _loadPerspective : function(perspective, then) {
    hui.ui.request({
      url: '/app/' + perspective.type.toLowerCase(),
      parameters: {
        id: perspective.id
      },
      $object : then,
      $failure : this._loadFailed.bind(this)
    })
  },
  _loadFailed : function() {
    this._changeFragment('error');
  },
  _viewNewItem : function(item) {
    list.refresh();
    this._pushState(item);
    if (item.type == 'InternetAddress') {
      this._onAddress(item);
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
          text: 'Realy remove?', element: rm,
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
  _onQuestion : function(data) {
    this._changeItem(data);
    hui.ui.get('questionHead').setContent(hui.build('h1.perspective_title',{text: data.text}))
    hui.ui.get('questionAnswers').setData(data.answers);
  },
  $render$questionAnswers : function(statement) {
    return this._render_relation(statement, {$remove: this._removeAnswerFromQuestion.bind(this)});
  },
  $select$questionAnswers : function(e) {
    this.show({ type:'Statement', id: e.data.id });
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
  _onStatement : function(data) {
    this._changeItem(data);
    hui.ui.get('statementHead').setContent(hui.build('h1.perspective_title',{text: data.text}))
    hui.ui.get('statementQuestions').setData(data.questions);
    hui.ui.get('statementAddresses').setData(data.addresses);
  },
  $render$statementQuestions : function(obj) {
    return this._render_relation(obj, {$remove: this._removeQuestionFromStatement.bind(this)});
  },
  $select$statementQuestions : function(e) {
    this.show({ type:'Question', id: e.data.id });
  },
  $render$statementAddresses : function(obj) {
    return this._render_relation(obj, {});
  },
  $select$statementAddresses : function(e) {
    this.show({ type:'InternetAddress', id: e.data.id });
  },
  $click$addQuestionToStatement : function() {
    hui.ui.get('questionFinder').show();
  },
  $select$questionFinder : function(obj) {
    hui.ui.get('questionFinder').hide();
    hui.ui.request({
      url: '/app/statement/add/question',
      parameters: {
        statementId: this._currentItem.id,
        questionId: obj.id
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
  _onHypothesis : function(data) {
    this._changeItem(data);
    hui.ui.get('hypothesisHead').setContent(hui.build('h1.perspective_title',{text: data.text}))
    hui.ui.get('hypothesisSupporting').setData(data.supports);
    hui.ui.get('hypothesisContradicting').setData(data.contradicts);
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
    hui.ui.request({ 
      url: '/app/hypothesis/remove/statement',
      parameters: {
        hypothesisId: this._currentItem.id,
        statementId: statement.id,
        relation: relation
      },
      $object : this._onHypothesis.bind(this)
    });
  },
  _addStatementToHypothesis : function(statement, relation) {
    hui.ui.request({ 
      url: '/app/hypothesis/add/statement',
      parameters: {
        hypothesisId: this._currentItem.id,
        statementId: statement.id,
        relation: relation
      },
      $object : this._onHypothesis.bind(this)
    });
  },


  // Address

  _loadAddress : function(address) {
    hui.ui.request({
      url: '/app/internetaddress',
      parameters: {
        id: address.id
      },
      $object : this._onAddress.bind(this),
      $failure: function() {}
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
  _onAddress : function(data) {
    this._changeItem(data);
    var fragment = hui.ui.get('internetaddress');
    fragment.setHTML('<div class="page reader_text">' + data.formatted + '</div>');
  },

  createStatementOnAddress : function(text) {
    hui.ui.request({
      url: '/app/internetaddress/create/statement',
      parameters: { id: this._currentItem.id, text: text },
      $object : function(obj) {
        this._onAddress(obj);
        list.refresh();
      }.bind(this)
    });
  },
}

hui.ui.listen(appController);
})();