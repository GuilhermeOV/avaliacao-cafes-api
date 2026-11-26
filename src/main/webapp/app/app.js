/*
 * FRONT-END da API de Cafés — JavaScript puro.
 *
 * Fluxo: o usuário clica -> função chama api() -> fetch() envia JSON para a API
 * -> a resposta (JSON + status HTTP) é desenhada na tela.
 *
 * O token JWT do login fica no localStorage e vai no cabeçalho
 * "Authorization: Bearer <token>" de toda requisição.
 */

// A página é servida pelo mesmo Tomcat em /cafes/app/, então a API está em /cafes/api.
const API = new URL('../api', location.href).href.replace(/\/$/, '');

// ------------------------------------------------------------------ sessão (login)

let sessao = JSON.parse(localStorage.getItem('sessao') || 'null');   // { token, usuario }

function salvarSessao(dados) {
    sessao = dados;
    if (dados) {
        localStorage.setItem('sessao', JSON.stringify(dados));
    } else {
        localStorage.removeItem('sessao');
    }
    desenharUsuario();
}

// ------------------------------------------------------------------ comunicação com a API

/**
 * Faz a requisição e devolve o JSON da resposta.
 * Se o status não for 2xx, lança um erro com a mensagem que a API mandou
 * no formato padrão { "status": 404, "erro": "..." }.
 */
async function api(metodo, caminho, corpo) {
    const opcoes = { method: metodo, headers: {} };
    if (corpo !== undefined) {
        opcoes.headers['Content-Type'] = 'application/json';
        opcoes.body = JSON.stringify(corpo);
    }
    if (sessao) {
        opcoes.headers['Authorization'] = 'Bearer ' + sessao.token;
    }

    const resposta = await fetch(API + caminho, opcoes);
    const dados = resposta.status === 204 ? null : await resposta.json().catch(() => null);

    if (!resposta.ok) {
        // Token vencido ou inválido: desloga para o usuário entrar de novo.
        if (resposta.status === 401 && sessao && !caminho.startsWith('/auth/')) {
            salvarSessao(null);
        }
        const erro = new Error(dados?.erro || 'Erro ' + resposta.status);
        erro.status = resposta.status;
        throw erro;
    }
    return dados;
}

// ------------------------------------------------------------------ avisos (toast)

function avisar(texto, tipo = 'ok') {
    const div = document.createElement('div');
    div.className = 'aviso ' + tipo;
    div.innerHTML = texto;
    document.getElementById('avisos').appendChild(div);
    setTimeout(() => div.remove(), 5000);
}

/** Mostra o erro da API com o status HTTP (ex.: "409 — Voce ja avaliou este cafe."). */
function avisarErro(erro) {
    const status = erro.status ? `<code>${erro.status}</code> ` : '';
    avisar(status + esc(erro.message), 'erro');
}

// ------------------------------------------------------------------ utilidades

/** Escapa HTML para que dados vindos da API não virem código na página. */
function esc(texto) {
    return String(texto ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
}

function estrelas(nota) {
    const cheias = Math.round(nota || 0);
    return '★'.repeat(cheias) + '☆'.repeat(5 - cheias);
}

function formatarNota(nota) {
    return nota == null ? '—' : Number(nota).toFixed(1).replace('.', ',');
}

function formatarData(iso) {
    return iso ? new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '';
}

function precisaLogin() {
    if (!sessao) {
        avisar('Entre com sua conta para fazer isso.', 'erro');
        abrirLogin();
        return true;
    }
    return false;
}

// ------------------------------------------------------------------ cabeçalho (usuário)

/** Desenha o botão do topo e o conteúdo do painel conforme está logado ou não. */
function desenharUsuario() {
    const botao = document.getElementById('btnMenu');
    const logado = !!sessao;

    document.getElementById('painelLogado').hidden = !logado;
    document.getElementById('formLogin').hidden = logado;

    if (logado) {
        const { nome, email } = sessao.usuario;
        const inicial = esc(nome.charAt(0).toUpperCase());
        const primeiroNome = esc(nome.split(' ')[0]);
        botao.innerHTML = `<span class="avatar">${inicial}</span> ${primeiroNome} ▾`;
        document.getElementById('avatarGrande').textContent = inicial;
        document.getElementById('nomeLogado').textContent = nome;
        document.getElementById('emailLogado').textContent = email;
        document.getElementById('expiraLogado').textContent = sessao.expiraEm
            ? 'Sessão válida até ' + new Date(sessao.expiraEm).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
            : '';
    } else {
        botao.innerHTML = 'Entrar ▾';
    }
}

// ------------------------------------------------------------------ menu (abrir / fechar)

const painel = document.getElementById('painelMenu');

function abrirMenu() {
    painel.hidden = false;
    document.getElementById('btnMenu').setAttribute('aria-expanded', 'true');
    if (!sessao) document.querySelector('#formLogin [name=email]').focus();
}

function fecharMenu() {
    painel.hidden = true;
    document.getElementById('btnMenu').setAttribute('aria-expanded', 'false');
}

document.getElementById('btnMenu').onclick = (e) => {
    e.stopPropagation();
    painel.hidden ? abrirMenu() : fecharMenu();
};
// Clicar fora do painel ou apertar Esc fecha o menu
document.addEventListener('click', (e) => {
    if (!painel.hidden && !e.target.closest('.menu-usuario')) fecharMenu();
});
document.addEventListener('keydown', (e) => { if (e.key === 'Escape') fecharMenu(); });

document.getElementById('btnSair').onclick = () => {
    salvarSessao(null);
    fecharMenu();
    avisar('Você saiu.');
    recarregarDetalheAberto();
};

// ------------------------------------------------------------------ lista de cafés

async function carregarCafes() {
    // Filtros -> query string: /cafes?bairro=Centro&tipo=espresso&ordenar=media
    const params = new URLSearchParams();
    const bairro = document.getElementById('filtroBairro').value.trim();
    const tipo = document.getElementById('filtroTipo').value.trim();
    if (bairro) params.set('bairro', bairro);
    if (tipo) params.set('tipo', tipo);
    params.set('ordenar', document.getElementById('filtroOrdenar').value);

    const lista = document.getElementById('listaCafes');
    try {
        const cafes = await api('GET', '/cafes?' + params);
        if (cafes.length === 0) {
            lista.innerHTML = '<p class="vazio">Nenhum café encontrado com esses filtros.</p>';
            return;
        }
        const ranking = params.get('ordenar') === 'media';
        lista.innerHTML = cafes.map((c, i) => `
            <article class="cartao" data-id="${c.id}">
                ${ranking ? `<div class="posicao">#${i + 1} no ranking</div>` : ''}
                <h3>${esc(c.nome)}</h3>
                <div class="suave">${esc(c.endereco)} · ${esc(c.bairro)}</div>
                <p><span class="etiqueta">${esc(c.tipoCafePrincipal)}</span></p>
                ${c.totalReviews > 0
                    ? `<span class="nota-geral">${formatarNota(c.mediaGeral)}</span>
                       <span class="estrelas">${estrelas(c.mediaGeral)}</span>
                       <div class="suave">${c.totalReviews} avaliação(ões)</div>`
                    : '<div class="suave">Ainda sem avaliações</div>'}
            </article>`).join('');
        lista.querySelectorAll('.cartao').forEach(el => el.onclick = () => abrirCafe(el.dataset.id));
    } catch (e) {
        lista.innerHTML = `<p class="vazio">Não foi possível carregar os cafés: ${esc(e.message)}<br>
                           Verifique se a API está rodando (docker compose up -d).</p>`;
    }
}

// ------------------------------------------------------------------ detalhe do café + reviews

let cafeAbertoId = null;

async function abrirCafe(id) {
    cafeAbertoId = id;
    try {
        // As duas consultas são independentes, então rodam em paralelo.
        const [cafe, reviews] = await Promise.all([
            api('GET', `/cafes/${id}`),
            api('GET', `/cafes/${id}/reviews`)
        ]);
        desenharCafe(cafe, reviews);
        const dlg = document.getElementById('dlgCafe');
        if (!dlg.open) dlg.showModal();
    } catch (e) {
        avisarErro(e);
    }
}

function linhaMedia(rotulo, valor) {
    const largura = valor ? (valor / 5) * 100 : 0;
    return `<span>${rotulo}</span><div class="barra"><span style="width:${largura}%"></span></div>
            <strong>${formatarNota(valor)}</strong>`;
}

function desenharCafe(cafe, reviews) {
    const jaAvaliou = sessao && reviews.some(r => r.usuarioId === sessao.usuario.id);

    document.getElementById('conteudoCafe').innerHTML = `
        <h2>${esc(cafe.nome)}</h2>
        <div class="suave">${esc(cafe.endereco)} · ${esc(cafe.bairro)}</div>
        <p><span class="etiqueta">${esc(cafe.tipoCafePrincipal)}</span></p>

        ${cafe.totalReviews > 0 ? `
            <span class="nota-geral">${formatarNota(cafe.mediaGeral)}</span>
            <span class="estrelas">${estrelas(cafe.mediaGeral)}</span>
            <span class="suave">(${cafe.totalReviews} avaliação(ões))</span>
            <div class="medias">
                ${linhaMedia('Bebida', cafe.mediaBebida)}
                ${linhaMedia('Ambiente', cafe.mediaAmbiente)}
                ${linhaMedia('Atendimento', cafe.mediaAtendimento)}
            </div>` : '<p class="suave">Ainda sem avaliações.</p>'}

        <div class="acoes">
            <button class="perigo" id="btnExcluirCafe">Excluir café</button>
            <button id="btnEditarCafe">Editar café</button>
            ${jaAvaliou ? '' : '<button class="destaque" id="btnAvaliar">Avaliar</button>'}
        </div>

        <h3>Avaliações</h3>
        ${reviews.length === 0 ? '<p class="suave">Seja o primeiro a avaliar.</p>' : reviews.map(r => `
            <div class="review">
                <div class="cabecalho">
                    <strong>${esc(r.autor)}</strong>
                    <span class="suave">${formatarData(r.criadoEm)}</span>
                </div>
                <div class="suave">
                    Bebida <span class="estrelas">${estrelas(r.notaBebida)}</span> ·
                    Ambiente <span class="estrelas">${estrelas(r.notaAmbiente)}</span> ·
                    Atendimento <span class="estrelas">${estrelas(r.notaAtendimento)}</span>
                </div>
                ${r.comentario ? `<p>${esc(r.comentario)}</p>` : ''}
                ${sessao && r.usuarioId === sessao.usuario.id ? `
                    <div class="acoes">
                        <button class="perigo" data-excluir-review="${r.id}">Excluir</button>
                        <button data-editar-review="${r.id}">Editar</button>
                    </div>` : ''}
            </div>`).join('')}

        <div class="acoes"><button class="secundario" data-fechar>Fechar</button></div>`;

    // Liga os botões do detalhe
    const conteudo = document.getElementById('conteudoCafe');
    conteudo.querySelector('[data-fechar]').onclick = () => document.getElementById('dlgCafe').close();
    conteudo.querySelector('#btnEditarCafe').onclick = () => abrirFormCafe(cafe);
    conteudo.querySelector('#btnExcluirCafe').onclick = () => excluirCafe(cafe);
    const btnAvaliar = conteudo.querySelector('#btnAvaliar');
    if (btnAvaliar) btnAvaliar.onclick = () => abrirFormReview(cafe.id, null);
    conteudo.querySelectorAll('[data-editar-review]').forEach(b => b.onclick = () =>
        abrirFormReview(cafe.id, reviews.find(r => r.id == b.dataset.editarReview)));
    conteudo.querySelectorAll('[data-excluir-review]').forEach(b => b.onclick = () =>
        excluirReview(cafe.id, b.dataset.excluirReview));
}

// ------------------------------------------------------------------ login / cadastro

let modoCadastro = false;

function abrirLogin() {
    modoCadastro = false;
    atualizarModoLogin();
    // Se um diálogo estiver aberto (ex.: detalhe do café), fecha para o menu ficar visível
    document.querySelectorAll('dialog[open]').forEach(d => d.close());
    // setTimeout: espera o clique atual terminar, senão o "clicar fora" fecharia o menu na hora
    setTimeout(abrirMenu, 0);
}

function atualizarModoLogin() {
    document.getElementById('tituloLogin').textContent = modoCadastro ? 'Criar conta' : 'Entrar';
    document.getElementById('btnEnviarLogin').textContent = modoCadastro ? 'Cadastrar' : 'Entrar';
    document.getElementById('btnTrocarModo').textContent =
        modoCadastro ? 'Já tem conta? Entrar' : 'Não tem conta? Cadastre-se';
    document.getElementById('campoNome').hidden = !modoCadastro;
    document.querySelector('#formLogin [name=nome]').required = modoCadastro;
}

document.getElementById('btnTrocarModo').onclick = () => {
    modoCadastro = !modoCadastro;
    atualizarModoLogin();
};

document.getElementById('formLogin').onsubmit = async (evento) => {
    evento.preventDefault();
    const form = new FormData(evento.target);
    const email = form.get('email');
    const senha = form.get('senha');
    try {
        if (modoCadastro) {
            await api('POST', '/auth/registrar', { nome: form.get('nome'), email, senha });   // 201
            avisar('Conta criada!');
        }
        const resposta = await api('POST', '/auth/login', { email, senha });                // 200 + token
        salvarSessao({
            token: resposta.token,
            usuario: resposta.usuario,
            expiraEm: Date.now() + resposta.expiraEmHoras * 60 * 60 * 1000
        });
        fecharMenu();
        // Volta o formulário para o modo "Entrar" (para a próxima vez que sair e entrar)
        modoCadastro = false;
        atualizarModoLogin();
        evento.target.elements.nome.value = '';
        avisar(`Bem-vindo, ${esc(resposta.usuario.nome)}! Token válido por ${resposta.expiraEmHoras}h.`);
        recarregarDetalheAberto();
    } catch (e) {
        avisarErro(e);
    }
};

// ------------------------------------------------------------------ criar / editar / excluir café

let cafeEditando = null;

function abrirFormCafe(cafe) {
    if (precisaLogin()) return;
    cafeEditando = cafe;
    const form = document.getElementById('formCafe');
    form.reset();
    document.getElementById('tituloFormCafe').textContent = cafe ? 'Editar café' : 'Novo café';
    if (cafe) {
        ['nome', 'endereco', 'bairro', 'tipoCafePrincipal'].forEach(c => form.elements[c].value = cafe[c] ?? '');
    }
    document.getElementById('dlgFormCafe').showModal();
}

document.getElementById('btnNovoCafe').onclick = () => abrirFormCafe(null);

document.getElementById('formCafe').onsubmit = async (evento) => {
    evento.preventDefault();
    const dados = Object.fromEntries(new FormData(evento.target));
    try {
        if (cafeEditando) {
            await api('PUT', `/cafes/${cafeEditando.id}`, dados);       // 200
            avisar('<code>200</code> Café atualizado.');
        } else {
            const novo = await api('POST', '/cafes', dados);            // 201 Created
            avisar(`<code>201</code> Café "${esc(novo.nome)}" criado.`);
        }
        document.getElementById('dlgFormCafe').close();
        carregarCafes();
        recarregarDetalheAberto();
    } catch (e) {
        avisarErro(e);
    }
};

async function excluirCafe(cafe) {
    if (precisaLogin()) return;
    if (!confirm(`Excluir o café "${cafe.nome}"?`)) return;
    try {
        await api('DELETE', `/cafes/${cafe.id}`);                       // 204 (ou 409 se tiver reviews)
        avisar('<code>204</code> Café excluído.');
        document.getElementById('dlgCafe').close();
        carregarCafes();
    } catch (e) {
        avisarErro(e);
    }
}

// ------------------------------------------------------------------ criar / editar / excluir review

let reviewCafeId = null;
let reviewEditando = null;

// Preenche os <select> de nota com 1 a 5
document.querySelectorAll('#formReview select').forEach(sel => {
    sel.innerHTML = [5, 4, 3, 2, 1].map(n => `<option value="${n}">${n} ${'★'.repeat(n)}</option>`).join('');
});

function abrirFormReview(cafeId, review) {
    if (precisaLogin()) return;
    reviewCafeId = cafeId;
    reviewEditando = review;
    const form = document.getElementById('formReview');
    form.reset();
    document.getElementById('tituloFormReview').textContent = review ? 'Editar avaliação' : 'Avaliar café';
    if (review) {
        ['notaBebida', 'notaAmbiente', 'notaAtendimento', 'comentario'].forEach(c => form.elements[c].value = review[c] ?? '');
    }
    document.getElementById('dlgFormReview').showModal();
}

document.getElementById('formReview').onsubmit = async (evento) => {
    evento.preventDefault();
    const f = evento.target.elements;
    // O autor NÃO é enviado: a API pega do token JWT.
    const dados = {
        notaBebida: Number(f.notaBebida.value),
        notaAmbiente: Number(f.notaAmbiente.value),
        notaAtendimento: Number(f.notaAtendimento.value),
        comentario: f.comentario.value.trim()
    };
    try {
        if (reviewEditando) {
            await api('PUT', `/reviews/${reviewEditando.id}`, dados);                     // 200 (403 se não for o autor)
            avisar('<code>200</code> Avaliação atualizada.');
        } else {
            await api('POST', '/reviews', { cafeId: reviewCafeId, ...dados });            // 201 (409 se já avaliou)
            avisar('<code>201</code> Avaliação registrada!');
        }
        document.getElementById('dlgFormReview').close();
        abrirCafe(reviewCafeId);
        carregarCafes();
    } catch (e) {
        avisarErro(e);
    }
};

async function excluirReview(cafeId, reviewId) {
    if (!confirm('Excluir sua avaliação?')) return;
    try {
        await api('DELETE', `/reviews/${reviewId}`);                     // 204
        avisar('<code>204</code> Avaliação excluída.');
        abrirCafe(cafeId);
        carregarCafes();
    } catch (e) {
        avisarErro(e);
    }
}

// ------------------------------------------------------------------ inicialização

/** Depois de login/edição, redesenha o café aberto (ex.: para mostrar botões do autor). */
function recarregarDetalheAberto() {
    const dlg = document.getElementById('dlgCafe');
    if (dlg.open && cafeAbertoId) abrirCafe(cafeAbertoId);
}

// Botões "Cancelar" de todos os diálogos
document.querySelectorAll('dialog [data-fechar]').forEach(b => b.onclick = () => b.closest('dialog').close());

document.getElementById('btnFiltrar').onclick = carregarCafes;
document.getElementById('filtroOrdenar').onchange = carregarCafes;
document.getElementById('btnLimpar').onclick = () => {
    document.getElementById('filtroBairro').value = '';
    document.getElementById('filtroTipo').value = '';
    document.getElementById('filtroOrdenar').value = 'nome';
    carregarCafes();
};
document.querySelectorAll('.filtros input').forEach(i =>
    i.addEventListener('keydown', e => { if (e.key === 'Enter') carregarCafes(); }));

desenharUsuario();
carregarCafes();
