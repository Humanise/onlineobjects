hui.control({
  '#name' : 'questions',
  nodes : {
    intel: '.js-question-intel',
    title: '#questionTitleText'
  },
  components: {
    answers: 'questionAnswers',
    words: 'questionWords',
    tags: 'questionTags',
    suggestionsFragment: 'questionSuggestionsFragment',
    suggestions: 'questionSuggestions',
    answerFinder: 'answerFinder',
  },
  state : {
    id: null
  },
  show(data) {
    hui.dom.setText(this.nodes.title, data.text);
    this.components.answers.setData(data.answers);
    this.components.words.setData(data.words);
    this.components.tags.setData(data.tags);
    this.nodes.intel.style.display = 'none';
    const changed = data.id !== this.state.id;
    this.state.id = data.id;
    if (changed) {
      this._fetchSuggestions(data.id);
    }
  },

  'addAnswerToQuestion.click!'() {
    this.components.answerFinder.show();
  },
  'answerFinder.select!'(e) {
    this.components.answerFinder.hide();
    this._addAnswerToQuestion(e.value)
  },
  'questionAnswers.render!'(e) {
    return appController._render_relation(e.value, { $remove: this._removeAnswerFromQuestion.bind(this) });
  },
  $select$questionAnswers : function(e) {
    appController.show(e.data);
  },
  _removeAnswerFromQuestion(answer) {
    appController._request({
      url: '/app/question/remove/answer',
      parameters: {
        answerId: answer.id,
        answerType: answer.type,
        questionId: this.state.id
      }
    }).then((q) => appController._onQuestion(q));
  },
  _addAnswerToQuestion : function(statement) {
    return appController._request({
      url: '/app/question/add/answer',
      parameters: {
        answerId: statement.id,
        answerType: statement.kind,
        questionId: this.state.id
      }
    }).then(appController._onQuestion.bind(appController));
  },

  'answerQuestion.click!'() {
    var item = appController.getCurrentItem();
    var url = '/app/question/answer?id=' + item.id;
    var output = this.nodes.intel;
    output.innerText = 'Let me think...';
    output.style.display = '';
    // TODO: Handle if the questions goes away
    oo.intelligence.stream({url: url, $html: (s) => {
      output.innerHTML = s;
    }})
  },
  _fetchSuggestions() {
    hui.ui.request({
      url: '/app/question/suggest/statement?id=' + this.state.id,
      $object: (result) => {
        this.components.suggestionsFragment.show();
        this.components.suggestions.setData(result.suggestions);
      }
    });
  },
  'questionSuggestions.render!'(e) {
    const obj = e.value;
    const add = hui.build('a.perspective_relation_add', { href: '#add' });
    hui.on(add, 'click', (e) => {
      e.stopPropagation();
      e.preventDefault();
      this._addAnswerToQuestion({id: obj.entity.id, kind: 'Statement'}).then(() => this._fetchSuggestions())
    })
    return hui.build('div.perspective_relation perspective_relation-statement', {children:[
      add,
      hui.build('div.perspective_relation_title',{text: obj.description + " - " + Math.round(obj.strength * 100) + '%'})
    ]});
  },
  'questionSuggestions.select!'(e) {
    const entity = e.value.data.entity;
    appController.show({id: entity.id, text: entity.name, type: 'Statement'});
    return
    console.log(e)
    this._addAnswerToQuestion({id: e.value.data.entity.id, kind: 'Statement'}).then(() => this._fetchSuggestions())
  }
});