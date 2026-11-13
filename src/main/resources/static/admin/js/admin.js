/* =====================================================================
   Painel admin · abraao.dev — comportamento das telas
   Sem dependências. Cada bloco só roda se a tela tiver o elemento dele.
   ===================================================================== */
(function () {
  'use strict';

  var $ = function (sel, raiz) { return (raiz || document).querySelector(sel); };
  var $$ = function (sel, raiz) { return Array.prototype.slice.call((raiz || document).querySelectorAll(sel)); };

  var SVG_X = '<svg aria-hidden="true" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M6 6l12 12M18 6 6 18"></path></svg>';

  /* ---------------------------------------------------------------
     1. Enter em campo de texto não envia o formulário (só no botão)
     --------------------------------------------------------------- */
  $$('form[data-sem-enter]').forEach(function (form) {
    form.addEventListener('keydown', function (e) {
      var t = e.target;
      if (e.key !== 'Enter' || t.tagName !== 'INPUT') return;
      if (t.type === 'submit' || t.type === 'button' || t.type === 'checkbox' || t.type === 'radio') return;
      e.preventDefault();
    });
  });

  /* ---------------------------------------------------------------
     2. Confirmação antes de ações sem volta (excluir)
     --------------------------------------------------------------- */
  document.addEventListener('click', function (e) {
    var alvo = e.target.closest('[data-confirmar]');
    if (alvo && !window.confirm(alvo.getAttribute('data-confirmar'))) {
      e.preventDefault();
      e.stopImmediatePropagation();
    }
  }, true);

  /* ---------------------------------------------------------------
     3. Aviso ao sair com alterações não salvas
     --------------------------------------------------------------- */
  var sujo = false;
  var enviando = false;
  $$('form[data-avisar-saida]').forEach(function (form) {
    form.addEventListener('input', function () { sujo = true; });
    form.addEventListener('change', function () { sujo = true; });
  });
  document.addEventListener('submit', function () { enviando = true; }, true);
  window.addEventListener('beforeunload', function (e) {
    if (sujo && !enviando) { e.preventDefault(); e.returnValue = ''; }
  });
  window.__adminMarcarSujo = function () { sujo = true; };

  /* ---------------------------------------------------------------
     4. Contador de caracteres (textarea[data-contador="#id"])
     --------------------------------------------------------------- */
  $$('[data-contador]').forEach(function (campo) {
    var saida = $(campo.getAttribute('data-contador'));
    if (!saida) return;
    var max = parseInt(campo.getAttribute('maxlength'), 10) || 120;
    var atualizar = function () {
      var n = campo.value.length;
      saida.textContent = n + '/' + max;
      saida.classList.toggle('estourou', n >= max);
    };
    campo.addEventListener('input', atualizar);
    atualizar();
  });

  /* ---------------------------------------------------------------
     5. Chips (tags do projeto, tecnologias da fase)
        O <input data-chips-fonte> guarda "A, B, C" — é ele que vai pro servidor.
        Com JS, ele some e a caixa [data-chips-ui] mostra os chips.
     --------------------------------------------------------------- */
  $$('[data-chips-fonte]').forEach(function (fonte) {
    var nome = fonte.getAttribute('data-chips-fonte');
    var ui = $('[data-chips-ui="' + nome + '"]');
    if (!ui) return;
    var entrada = $('input', ui);
    var max = parseInt(fonte.getAttribute('data-max'), 10) || 8;
    var lista = fonte.value.split(',').map(function (s) { return s.trim(); }).filter(Boolean);

    if (fonte.classList.contains('in--erro')) ui.style.borderColor = 'var(--erro)';
    fonte.type = 'hidden';
    ui.hidden = false;
    ui.addEventListener('click', function (e) { if (e.target === ui) entrada.focus(); });

    var sincronizar = function () {
      fonte.value = lista.join(', ');
      entrada.placeholder = lista.length >= max ? 'Limite de ' + max + ' atingido' : entrada.getAttribute('data-placeholder');
      entrada.readOnly = lista.length >= max;
      if (window.__adminMarcarSujo) window.__adminMarcarSujo();
    };
    entrada.setAttribute('data-placeholder', entrada.placeholder);

    var desenhar = function () {
      $$('.tag', ui).forEach(function (t) { t.remove(); });
      lista.forEach(function (valor, i) {
        var tag = document.createElement('span');
        tag.className = 'tag';
        var texto = document.createElement('span');
        texto.textContent = valor;
        var x = document.createElement('button');
        x.type = 'button';
        x.className = 'chip__x';
        x.setAttribute('aria-label', 'Remover ' + valor);
        x.innerHTML = SVG_X;
        x.addEventListener('click', function () {
          lista.splice(i, 1);
          desenhar();
          sincronizar();
          entrada.focus();
        });
        tag.appendChild(texto);
        tag.appendChild(x);
        ui.insertBefore(tag, entrada);
      });
    };

    var adicionar = function () {
      var valor = entrada.value.replace(/,/g, ' ').trim().slice(0, 30);
      entrada.value = '';
      if (!valor || lista.length >= max) return;
      var existe = lista.some(function (v) { return v.toLowerCase() === valor.toLowerCase(); });
      if (!existe) { lista.push(valor); desenhar(); sincronizar(); }
    };

    entrada.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' || e.key === ',') { e.preventDefault(); adicionar(); }
      else if (e.key === 'Backspace' && !entrada.value && lista.length) {
        lista.pop(); desenhar(); sincronizar();
      }
    });
    entrada.addEventListener('blur', adicionar);
    entrada.addEventListener('change', function () {
      // escolher uma sugestão do <datalist> conta como Enter
      if (entrada.value && entrada.list && $$('option', entrada.list).some(function (o) { return o.value === entrada.value; })) adicionar();
    });

    desenhar();
    fonte.value = lista.join(', ');
  });

  /* ---------------------------------------------------------------
     6. Projeto: pré-visualização ao vivo, ícone e cor
     --------------------------------------------------------------- */
  var formProjeto = $('form[data-projeto]');
  if (formProjeto) {
    var preencher = function (chave, valor) {
      $$('[data-prev="' + chave + '"]').forEach(function (el) {
        el.textContent = valor && String(valor).trim() ? valor : el.getAttribute('data-vazio');
      });
    };

    formProjeto.addEventListener('input', function (e) {
      var fonte = e.target.getAttribute('data-prev-fonte');
      if (fonte === 'nome' || fonte === 'ano' || fonte === 'desc') preencher(fonte, e.target.value);
    });

    formProjeto.addEventListener('change', function (e) {
      var alvo = e.target;
      var fonte = alvo.getAttribute('data-prev-fonte');
      if (fonte === 'status') {
        preencher('status', alvo.nextElementSibling ? alvo.nextElementSibling.textContent : '');
      } else if (fonte === 'icone') {
        var d = alvo.getAttribute('data-path');
        $$('[data-prev-icone]').forEach(function (p) { p.setAttribute('d', d); });
      } else if (fonte === 'cor') {
        var face = alvo.getAttribute('data-face');
        var sombra = alvo.getAttribute('data-sombra');
        $$('[data-prev-cor], [data-icones]').forEach(function (el) {
          el.style.setProperty('--face', face);
          el.style.setProperty('--sombra', sombra);
        });
        var texto = $('[data-cor-texto]');
        if (texto) texto.textContent = alvo.getAttribute('data-rotulo') + ' · ' + face;
      }
    });
  }

  /* ---------------------------------------------------------------
     7. Prints: arrastar e soltar, validação e miniaturas
     --------------------------------------------------------------- */
  var inputPrints = $('input[data-prints]');
  if (inputPrints) {
    var zona = $('[data-dropzone]');
    var listaPrints = $('[data-prints-lista]');
    var erroPrints = $('[data-prints-erro]');
    var MAX = parseInt(inputPrints.getAttribute('data-max'), 10) || 5;
    var MAX_BYTES = parseInt(inputPrints.getAttribute('data-max-bytes'), 10) || 2097152;
    var TIPOS = ['image/png', 'image/jpeg', 'image/webp'];
    var escolhidos = [];
    var urls = [];

    var salvosRestantes = function () {
      var total = parseInt(inputPrints.getAttribute('data-salvos'), 10) || 0;
      return total - $$('[data-remover-print]:checked').length;
    };

    var mostrarErro = function (msg) {
      erroPrints.textContent = msg || '';
      erroPrints.hidden = !msg;
    };

    var aplicar = function () {
      var dt = new DataTransfer();
      escolhidos.forEach(function (f) { dt.items.add(f); });
      inputPrints.files = dt.files;

      urls.forEach(function (u) { URL.revokeObjectURL(u); });
      urls = [];
      listaPrints.innerHTML = '';
      escolhidos.forEach(function (arquivo, i) {
        var url = URL.createObjectURL(arquivo);
        urls.push(url);
        var li = document.createElement('li');
        li.className = 'print';
        var img = document.createElement('img');
        img.src = url;
        img.alt = '';
        li.appendChild(img);
        if (i === 0 && salvosRestantes() === 0) {
          var capa = document.createElement('span');
          capa.className = 'print__capa mono';
          capa.textContent = 'capa';
          li.appendChild(capa);
        }
        var info = document.createElement('span');
        info.className = 'print__info';
        info.textContent = arquivo.name;
        li.appendChild(info);
        var tirar = document.createElement('button');
        tirar.type = 'button';
        tirar.className = 'print__tirar';
        tirar.textContent = 'Tirar';
        tirar.addEventListener('click', function () {
          escolhidos.splice(i, 1);
          aplicar();
        });
        li.appendChild(tirar);
        listaPrints.appendChild(li);
      });
      listaPrints.hidden = escolhidos.length === 0;
      if (window.__adminMarcarSujo) window.__adminMarcarSujo();
    };

    var receber = function (arquivos) {
      var problemas = [];
      Array.prototype.forEach.call(arquivos, function (f) {
        if (TIPOS.indexOf(f.type) === -1) { problemas.push(f.name + ': use PNG, JPG ou WebP.'); return; }
        if (f.size > MAX_BYTES) { problemas.push(f.name + ': passa de 2 MB.'); return; }
        if (salvosRestantes() + escolhidos.length >= MAX) { problemas.push('Máximo de ' + MAX + ' prints por projeto.'); return; }
        var repetido = escolhidos.some(function (e) { return e.name === f.name && e.size === f.size; });
        if (!repetido) escolhidos.push(f);
      });
      mostrarErro(problemas.filter(function (p, i, a) { return a.indexOf(p) === i; }).join(' '));
      aplicar();
    };

    inputPrints.addEventListener('change', function () {
      var novos = Array.prototype.slice.call(inputPrints.files);
      receber(novos);
    });

    ['dragenter', 'dragover'].forEach(function (ev) {
      zona.addEventListener(ev, function (e) { e.preventDefault(); zona.classList.add('dropzone--arrastando'); });
    });
    ['dragleave', 'drop'].forEach(function (ev) {
      zona.addEventListener(ev, function (e) { e.preventDefault(); zona.classList.remove('dropzone--arrastando'); });
    });
    zona.addEventListener('drop', function (e) {
      if (e.dataTransfer && e.dataTransfer.files) receber(e.dataTransfer.files);
    });
    zona.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); inputPrints.click(); }
    });
    $$('[data-remover-print]').forEach(function (c) { c.addEventListener('change', function () { mostrarErro(''); aplicar(); }); });
  }

  /* ---------------------------------------------------------------
     8. Fase: "Ainda estou nessa fase" desliga o campo Fim; máscara mm/aaaa
     --------------------------------------------------------------- */
  var emAndamento = $('[data-em-andamento]');
  var campoFim = $('[data-fim]');
  if (emAndamento && campoFim) {
    var guardado = campoFim.value;
    var alternar = function () {
      if (emAndamento.checked) {
        guardado = campoFim.value || guardado;
        campoFim.value = '';
        campoFim.disabled = true;
        campoFim.placeholder = 'hoje';
      } else {
        campoFim.disabled = false;
        campoFim.placeholder = 'mm/aaaa';
        if (!campoFim.value) campoFim.value = guardado || '';
      }
    };
    emAndamento.addEventListener('change', alternar);
    alternar();
  }

  $$('[data-mes-ano]').forEach(function (campo) {
    campo.addEventListener('input', function () {
      var d = campo.value.replace(/\D/g, '').slice(0, 6);
      campo.value = d.length > 2 ? d.slice(0, 2) + '/' + d.slice(2) : d;
    });
  });

  /* ---------------------------------------------------------------
     9. Texto da "Próxima fase": o botão Salvar só aparece se mudou
     --------------------------------------------------------------- */
  var formProxima = $('form[data-proxima]');
  if (formProxima) {
    var campoProxima = $('input[name="texto"]', formProxima);
    var botaoProxima = $('[data-proxima-salvar]', formProxima);
    var original = campoProxima.value;
    campoProxima.addEventListener('input', function () {
      botaoProxima.hidden = campoProxima.value.trim() === original.trim();
    });
    campoProxima.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') { campoProxima.value = original; botaoProxima.hidden = true; }
    });
  }

  /* ---------------------------------------------------------------
     10. Stack: chips, categorias novas, pendências, descartar
     --------------------------------------------------------------- */
  var formStack = $('form[data-stack]');
  if (formStack) {
    var caixa = $('[data-categorias]', formStack);
    var modeloCategoria = $('template[data-modelo-categoria]');
    var modeloChip = $('template[data-modelo-chip]');
    var listaPend = $('[data-pendencias]', formStack);
    var semPend = $('[data-sem-pendencias]', formStack);
    var htmlInicial = caixa.innerHTML;

    var ler = function () {
      return $$('[data-categoria]', caixa).map(function (cat) {
        return {
          id: $('[data-campo="id"]', cat).value,
          titulo: $('[data-campo="title"]', cat).value.trim(),
          itens: $$('[data-chip] [data-nome]', cat).map(function (n) { return n.textContent.trim(); })
        };
      });
    };
    var antes = ler();

    var reindexar = function () {
      $$('[data-categoria]', caixa).forEach(function (cat, i) {
        $('[data-campo="id"]', cat).name = 'categorias[' + i + '].id';
        $('[data-campo="title"]', cat).name = 'categorias[' + i + '].title';
        $$('[data-campo="items"]', cat).forEach(function (h) { h.name = 'categorias[' + i + '].items'; });
        var conta = $('[data-conta]', cat);
        var n = $$('[data-chip]', cat).length;
        if (conta) conta.textContent = n;
        var rot = $('[data-conta-rotulo]', cat);
        if (rot) rot.textContent = n === 1 ? 'item' : 'itens';
        cat.classList.toggle('categoria--apagada', !$('[data-campo="title"]', cat).value.trim() && !!$('[data-campo="id"]', cat).value);
      });
      var vazio = $('[data-sem-categorias]', caixa);
      if (vazio) vazio.hidden = $$('[data-categoria]', caixa).length > 0;
    };

    var linha = function (classe, texto) {
      var li = document.createElement('li');
      li.className = classe;
      li.textContent = texto;
      listaPend.appendChild(li);
    };

    var pendencias = function () {
      var agora = ler();
      listaPend.innerHTML = '';
      var porId = {};
      antes.forEach(function (c) { porId[c.id] = c; });
      var idsAgora = {};

      agora.forEach(function (c) {
        if (c.id) idsAgora[c.id] = true;
        var velho = c.id ? porId[c.id] : null;
        if (!velho) {
          if (c.titulo || c.itens.length) linha('mais', '+ categoria “' + (c.titulo || 'sem nome') + '”');
          c.itens.forEach(function (it) { linha('mais', '+ ' + it); });
          return;
        }
        if (!c.titulo) { linha('menos', '− categoria “' + velho.titulo + '”'); return; }
        if (c.titulo !== velho.titulo) linha('muda', velho.titulo + ' → ' + c.titulo);
        velho.itens.filter(function (it) { return c.itens.indexOf(it) === -1; })
          .forEach(function (it) { linha('menos', '− ' + it); });
        c.itens.filter(function (it) { return velho.itens.indexOf(it) === -1; })
          .forEach(function (it) { linha('mais', '+ ' + it); });
      });
      antes.forEach(function (c) { if (!idsAgora[c.id]) linha('menos', '− categoria “' + c.titulo + '”'); });

      var tem = listaPend.children.length > 0;
      listaPend.hidden = !tem;
      semPend.hidden = tem;
      sujo = tem;
    };

    var atualizar = function () { reindexar(); pendencias(); };

    var adicionarChip = function (campo) {
      var valor = campo.value.trim().slice(0, 30);
      campo.value = '';
      if (!valor) return;
      var cat = campo.closest('[data-categoria]');
      var existe = $$('[data-nome]', cat).some(function (n) { return n.textContent.trim().toLowerCase() === valor.toLowerCase(); });
      if (existe) return;
      var chip = modeloChip.content.firstElementChild.cloneNode(true);
      $('[data-nome]', chip).textContent = valor;
      $('[data-campo="items"]', chip).value = valor;
      $('[data-remover]', chip).setAttribute('aria-label', 'Remover ' + valor);
      campo.parentNode.insertBefore(chip, campo);
      atualizar();
    };

    caixa.addEventListener('keydown', function (e) {
      if (e.target.matches('[data-adicionar]') && e.key === 'Enter') {
        e.preventDefault();
        adicionarChip(e.target);
      }
    });
    caixa.addEventListener('click', function (e) {
      var x = e.target.closest('[data-remover]');
      if (!x) return;
      var chip = x.closest('[data-chip]');
      var campo = $('[data-adicionar]', chip.parentNode);
      chip.remove();
      atualizar();
      if (campo) campo.focus();
    });
    caixa.addEventListener('input', function (e) {
      if (e.target.matches('[data-campo="title"]')) {
        var cat = e.target.closest('[data-categoria]');
        var add = $('[data-adicionar]', cat);
        if (add) add.setAttribute('aria-label', 'Adicionar em ' + (e.target.value.trim() || 'categoria'));
        atualizar();
      }
    });

    var botaoNova = $('[data-nova-categoria]');
    if (botaoNova) {
      botaoNova.addEventListener('click', function () {
        var nova = modeloCategoria.content.firstElementChild.cloneNode(true);
        var vazio = $('[data-sem-categorias]', caixa);
        caixa.insertBefore(nova, vazio || null);
        atualizar();
        var titulo = $('[data-campo="title"]', nova);
        nova.scrollIntoView({ behavior: 'smooth', block: 'center' });
        titulo.focus({ preventScroll: true });
      });
    }

    $('[data-descartar]', formStack).addEventListener('click', function () {
      caixa.innerHTML = htmlInicial;
      atualizar();
    });

    formStack.addEventListener('submit', function () {
      // texto digitado e não confirmado com Enter também entra
      $$('[data-adicionar]', caixa).forEach(function (campo) { if (campo.value.trim()) adicionarChip(campo); });
      // categoria nova sem nome e sem itens é ignorada
      $$('[data-categoria]', caixa).forEach(function (cat) {
        if (!$('[data-campo="id"]', cat).value && !$('[data-campo="title"]', cat).value.trim() && !$$('[data-chip]', cat).length) cat.remove();
      });
      reindexar();
      sujo = false;
    });

    atualizar();
  }
})();
