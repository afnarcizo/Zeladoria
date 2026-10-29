#!/usr/bin/env python3
"""
Gera o documento Word (.docx) nativo (OpenXML) com:
1. Visão Geral e Arquitetura (Java 21, Spring Boot 3, JPA, MySQL)
2. Regras de Negócio (RN01 a RN08)
3. Diagrama Entidade-Relacionamento (DER) com imagem de alta resolução embutida + representação estrutural
4. Dicionário de Dados Completo (tb_jpa_sequences, tb_categoria_problema, tb_imagem_solicitacao, tb_solicitacao_zeladoria)
5. Documentação Completa dos Endpoints REST (Swagger / OpenAPI 3 & API Blueprint)
"""

import os
import zipfile
import struct
from xml.sax.saxutils import escape

OUTPUT_DOCX = "/Development/Zeladoria/docs/Documentacao_Zeladoria_Urbana.docx"
DER_PNG_PATH = "/Development/Zeladoria/docs/der_zeladoria.png"


def get_png_dimensions(png_path):
    with open(png_path, "rb") as f:
        data = f.read(24)
        width, height = struct.unpack(">II", data[16:24])
        return width, height


def w_p(text="", style=None, bold=False, color=None, size_half_pts=None, spacing_after=120):
    pPr = ""
    if style or spacing_after is not None:
        style_xml = f'<w:pStyle w:val="{style}"/>' if style else ""
        space_xml = f'<w:spacing w:after="{spacing_after}"/>' if spacing_after is not None else ""
        pPr = f"<w:pPr>{style_xml}{space_xml}</w:pPr>"
    if not text:
        return f"<w:p>{pPr}</w:p>"
    rPr_parts = []
    if bold:
        rPr_parts.append("<w:b/>")
    if color:
        rPr_parts.append(f'<w:color w:val="{color}"/>')
    if size_half_pts:
        rPr_parts.append(f'<w:sz w:val="{size_half_pts}"/><w:szCs w:val="{size_half_pts}"/>')
    rPr = f"<w:rPr>{''.join(rPr_parts)}</w:rPr>" if rPr_parts else ""
    return f"<w:p>{pPr}<w:r>{rPr}<w:t xml:space=\"preserve\">{escape(text)}</w:t></w:r></w:p>"


def w_bullet(title_bold, text_body):
    return (
        "<w:p>"
        '<w:pPr><w:spacing w:after="80"/><w:ind w:left="360" w:hanging="180"/></w:pPr>'
        '<w:r><w:rPr><w:b/><w:color w:val="1E3A8A"/></w:rPr><w:t xml:space="preserve">• '
        + escape(title_bold)
        + "</w:t></w:r>"
        '<w:r><w:t xml:space="preserve">'
        + escape(text_body)
        + "</w:t></w:r>"
        "</w:p>"
    )


def w_table(headers, rows, header_bg="1E3A8A"):
    xml = [
        "<w:tbl>",
        "<w:tblPr>",
        '<w:tblW w:w="5000" w:type="pct"/>',
        "<w:tblBorders>",
        '<w:top w:val="single" w:sz="6" w:space="0" w:color="CBD5E1"/>',
        '<w:left w:val="single" w:sz="6" w:space="0" w:color="CBD5E1"/>',
        '<w:bottom w:val="single" w:sz="6" w:space="0" w:color="CBD5E1"/>',
        '<w:right w:val="single" w:sz="6" w:space="0" w:color="CBD5E1"/>',
        '<w:insideH w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>',
        '<w:insideV w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>',
        "</w:tblBorders>",
        "</w:tblPr>",
    ]
    # Header row
    xml.append("<w:tr>")
    for h in headers:
        xml.append(
            "<w:tc>"
            f'<w:tcPr><w:shd w:val="clear" w:color="auto" w:fill="{header_bg}"/>'
            '<w:tcMar><w:top w:w="80" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/>'
            '<w:left w:w="100" w:type="dxa"/><w:right w:w="100" w:type="dxa"/></w:tcMar></w:tcPr>'
            '<w:p><w:pPr><w:spacing w:after="0"/></w:pPr>'
            '<w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="19"/></w:rPr>'
            f'<w:t xml:space="preserve">{escape(h)}</w:t></w:r></w:p></w:tc>'
        )
    xml.append("</w:tr>")

    for idx, row in enumerate(rows):
        bg = "F8FAFC" if idx % 2 == 1 else "FFFFFF"
        xml.append("<w:tr>")
        for col_idx, cell in enumerate(row):
            is_bold = col_idx == 0
            b_tag = "<w:b/>" if is_bold else ""
            xml.append(
                "<w:tc>"
                f'<w:tcPr><w:shd w:val="clear" w:color="auto" w:fill="{bg}"/>'
                '<w:tcMar><w:top w:w="60" w:type="dxa"/><w:bottom w:w="60" w:type="dxa"/>'
                '<w:left w:w="100" w:type="dxa"/><w:right w:w="100" w:type="dxa"/></w:tcMar></w:tcPr>'
                '<w:p><w:pPr><w:spacing w:after="0"/></w:pPr>'
                f'<w:r><w:rPr>{b_tag}<w:sz w:val="18"/></w:rPr>'
                f'<w:t xml:space="preserve">{escape(str(cell))}</w:t></w:r></w:p></w:tc>'
            )
        xml.append("</w:tr>")

    xml.append("</w:tbl>")
    xml.append(w_p("", spacing_after=140))
    return "".join(xml)


def w_image(rel_id, width_px, height_px):
    # Max width in EMUs (~6.2 inches = 5669280 EMUs)
    max_cx = 5669280
    cx = max_cx
    cy = int(max_cx * (height_px / float(width_px)))
    return f"""
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:spacing w:before="120" w:after="160"/></w:pPr>
      <w:r>
        <w:drawing>
          <wp:inline distT="0" distB="0" distL="0" distR="0"
                     xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
                     xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                     xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
            <wp:extent cx="{cx}" cy="{cy}"/>
            <wp:docPr id="1" name="DER_Zeladoria"/>
            <a:graphic>
              <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
                <pic:pic>
                  <pic:nvPicPr>
                    <pic:cNvPr id="0" name="der_zeladoria.png"/>
                    <pic:cNvPicPr/>
                  </pic:nvPicPr>
                  <pic:blipFill>
                    <a:blip r:embed="{rel_id}"/>
                    <a:stretch><a:fillRect/></a:stretch>
                  </pic:blipFill>
                  <pic:spPr>
                    <a:xfrm><a:off x="0" y="0"/><a:ext cx="{cx}" cy="{cy}"/></a:xfrm>
                    <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
                  </pic:spPr>
                </pic:pic>
              </a:graphicData>
            </a:graphic>
          </wp:inline>
        </w:drawing>
      </w:r>
    </w:p>
    """


def build_document_xml(img_w, img_h):
    body = []

    # Cover / Header
    body.append(w_p("SISTEMA DE ZELADORIA URBANA - API RESTFUL", style="Title", bold=True, color="0F172A", size_half_pts=36, spacing_after=80))
    body.append(w_p("Especificação Técnica, Regras de Negócio, Dicionário de Dados, DER e Catálogo de Endpoints (Swagger & API Blueprint)", bold=True, color="475569", size_half_pts=22, spacing_after=240))

    # 1. Visão Geral e Arquitetura
    body.append(w_p("1. Visão Geral e Arquitetura do Sistema", style="Heading1", bold=True, color="1E3A8A", size_half_pts=28, spacing_after=120))
    body.append(w_p(
        "O Sistema de Zeladoria Urbana é um webservice RESTful desenvolvido em Java 21 com Spring Boot 3.3.5 que permite a qualquer cidadão relatar problemas encontrados na cidade — tais como buracos em logradouros, lâmpadas de iluminação pública queimadas, acúmulo irregular de lixo, necessidade de poda de árvores e danos à sinalização viária."
    ))
    body.append(w_bullet("Linguagem e Runtime: ", "Java 21 LTS (Records, Pattern Matching, Jakarta EE 10)."))
    body.append(w_bullet("Framework Web e Persistência: ", "Spring Boot 3.3.5 (Spring Web MVC, Spring Data JPA / Hibernate 6, Jakarta Bean Validation)."))
    body.append(w_bullet("Padrão de Nomenclatura (CamelCase com Prefixos de Tipo): ", "Tabelas em CamelCase (SolicitacaoZeladoria, ImagemSolicitacao, CategoriaProblema, JpaSequences) e todos os atributos de banco de dados e código Java com prefixos: int (inteiro), str (String), dtm (Date/DateTime), lng (Long), dbl (Double/Float) e bln (Boolean)."))
    body.append(w_bullet("Controle de Auto-Incremento pelo JPA: ", "Uso de @GeneratedValue(strategy = GenerationType.TABLE) e @TableGenerator na tabela dedicada JpaSequences (strSequenceName, lngNextVal), garantindo que o auto-incremento seja gerenciado pelo JPA e não pelo AUTO_INCREMENT nativo do SGBD."))
    body.append(w_bullet("Armazenamento de Imagem em Tabela Separada: ", "As imagens das ocorrências são persistidas na tabela exclusiva ImagemSolicitacao e vinculadas à solicitação via chave estrangeira (lngImagemId)."))
    body.append(w_bullet("Exclusão Sempre Lógica (Soft Delete): ", "Nenhuma exclusão física (DELETE FROM) ocorre no banco de dados. Todas as entidades utilizam @SQLDelete (blnExcluido = true, dtmExclusao = CURRENT_TIMESTAMP) e @SQLRestriction(\"blnExcluido = false\")."))
    body.append(w_bullet("Documentação Dupla de Endpoints: ", "Todos os endpoints possuem documentação viva em OpenAPI 3 / Swagger (docs/swagger/openapi.json e /swagger-ui.html) e em API Blueprint (docs/blueprint/api.apib)."))

    # 2. Regras de Negócio
    body.append(w_p("2. Regras de Negócio (RN)", style="Heading1", bold=True, color="1E3A8A", size_half_pts=28, spacing_after=120))
    regras_rows = [
        ["RN01", "Identificação e Contato do Solicitante", "Toda solicitação de zeladoria deve conter obrigatoriamente o telefone de contato (strTelefoneUsuario, máx. 25 caracteres) e um endereço de e-mail válido (strEmailUsuario, máx. 150 caracteres) do cidadão relator."],
        ["RN02", "Geolocalização Precisa", "Toda solicitação deve informar as coordenadas geográficas do problema: dblLatitude (entre -90.0 e +90.0) e dblLongitude (entre -180.0 e +180.0), além de um endereço/ponto de referência opcional (strEnderecoReferencia)."],
        ["RN03", "Registro Automático de Data e Status Inicial", "A data da solicitação (dtmSolicitacao) é atribuída automaticamente pelo sistema no momento do recebimento (@PrePersist) e toda nova solicitação inicia obrigatoriamente com strStatus = ABERTA."],
        ["RN04", "Classificação por Categoria Ativa", "Cada solicitação deve estar associada a uma categoria de problema previamente cadastrada e ativa (blnExcluido = false) através de lngCategoriaId. O cadastro de categorias não permite strNome duplicado entre registros ativos."],
        ["RN05", "Armazenamento de Imagem em Tabela Separada", "A imagem comprobatória do problema é obrigatória no relato, validada quanto à codificação Base64 (strDadosBase64), tem seu tamanho em bytes (lngTamanhoBytes) calculado automaticamente e é persistida na tabela separada ImagemSolicitacao."],
        ["RN06", "Geração de Chaves Primárias Controlada pelo JPA", "Nenhuma tabela utiliza AUTO_INCREMENT nativo do banco de dados. Todos os identificadores (lngId) são gerados e controlados pelo provedor JPA por meio da tabela de sequências JpaSequences (strSequenceName, lngNextVal)."],
        ["RN07", "Exclusão Estritamente Lógica (Nunca Física)", "A operação DELETE em qualquer recurso nunca remove fisicamente o registro. O Hibernate executa um UPDATE definindo blnExcluido = true e gravando dtmExclusao = CURRENT_TIMESTAMP. Consultas padrão filtram automaticamente registros com blnExcluido = false."],
        ["RN08", "Ciclo de Vida e Imutabilidade de Solicitações Finalizadas", "Uma solicitação percorre os valores de strStatus: ABERTA, EM_ANALISE, EM_ANDAMENTO, CONCLUIDA ou CANCELADA. Solicitações já finalizadas (CONCLUIDA ou CANCELADA) não permitem novas alterações de status (retornando HTTP 422)."]
    ]
    body.append(w_table(["Código", "Regra de Negócio", "Descrição e Implementação Técnica"], regras_rows))

    # 3. Diagrama Entidade-Relacionamento (DER)
    body.append(w_p("3. Diagrama Entidade-Relacionamento (DER)", style="Heading1", bold=True, color="1E3A8A", size_half_pts=28, spacing_after=120))
    body.append(w_p(
        "O modelo relacional abaixo demonstra as 3 tabelas de domínio em padrão CamelCase (CategoriaProblema, SolicitacaoZeladoria e ImagemSolicitacao) e a tabela de controle de sequências do JPA (JpaSequences), utilizando os prefixos padronizados (lng, str, dbl, dtm, bln)."
    ))
    body.append(w_image("rIdDerImage", img_w, img_h))

    # 4. Dicionário de Dados
    body.append(w_p("4. Dicionário de Dados (CamelCase com Prefixos: int, str, dtm, lng, dbl, bln)", style="Heading1", bold=True, color="1E3A8A", size_half_pts=28, spacing_after=120))

    body.append(w_p("4.1. Tabela: SolicitacaoZeladoria", style="Heading2", bold=True, color="0F766E", size_half_pts=23, spacing_after=80))
    body.append(w_p("Armazena os relatos de problemas urbanos enviados pelos cidadãos."))
    sol_cols = [
        ["lngId", "BIGINT (Long)", "Não", "PK", "Identificador único gerado pelo JPA via JpaSequences (seqSolicitacaoZeladoria)."],
        ["strTelefoneUsuario", "VARCHAR(25) (String)", "Não", "-", "Telefone de contato do cidadão que relatou o problema."],
        ["strEmailUsuario", "VARCHAR(150) (String)", "Não", "-", "Endereço de e-mail do cidadão relator."],
        ["dblLatitude", "DOUBLE (Double)", "Não", "-", "Latitude da coordenada geográfica do problema (-90.0 a +90.0)."],
        ["dblLongitude", "DOUBLE (Double)", "Não", "-", "Longitude da coordenada geográfica do problema (-180.0 a +180.0)."],
        ["strEnderecoReferencia", "VARCHAR(255) (String)", "Sim", "-", "Logradouro, número ou ponto de referência próximo à ocorrência."],
        ["dtmSolicitacao", "DATETIME(6) (LocalDateTime)", "Não", "-", "Data e hora exata em que a solicitação foi registrada."],
        ["lngCategoriaId", "BIGINT (Long)", "Não", "FK", "Chave estrangeira para CategoriaProblema(lngId)."],
        ["lngImagemId", "BIGINT (Long)", "Sim", "FK / UQ", "Chave estrangeira única para a tabela separada ImagemSolicitacao(lngId)."],
        ["strDescricao", "VARCHAR(1000) (String)", "Não", "-", "Descrição detalhada do problema relatado pelo cidadão."],
        ["strStatus", "VARCHAR(30) (String/Enum)", "Não", "-", "Status atual: ABERTA, EM_ANALISE, EM_ANDAMENTO, CONCLUIDA ou CANCELADA."],
        ["dtmAtualizacao", "DATETIME(6) (LocalDateTime)", "Sim", "-", "Data e hora da última alteração de status ou atualização do registro."],
        ["blnExcluido", "TINYINT(1) (Boolean)", "Não", "IDX", "Flag de exclusão lógica (false/0 = Ativo, true/1 = Excluído logicamente)."],
        ["dtmExclusao", "DATETIME(6) (LocalDateTime)", "Sim", "-", "Data e hora em que o registro sofreu exclusão lógica."]
    ]
    body.append(w_table(["Atributo (Java / BD)", "Tipo (MySQL / Java)", "Nulo?", "Restrição", "Descrição"], sol_cols, header_bg="0F766E"))

    body.append(w_p("4.2. Tabela: ImagemSolicitacao (Tabela Separada de Imagens)", style="Heading2", bold=True, color="7C3AED", size_half_pts=23, spacing_after=80))
    body.append(w_p("Armazena separadamente as imagens comprobatórias associadas às solicitações de zeladoria."))
    img_cols = [
        ["lngId", "BIGINT (Long)", "Não", "PK", "Identificador único gerado pelo JPA via JpaSequences (seqImagemSolicitacao)."],
        ["strNomeArquivo", "VARCHAR(255) (String)", "Não", "-", "Nome original do arquivo de imagem enviado (ex.: buraco.jpg)."],
        ["strTipoConteudo", "VARCHAR(100) (String)", "Não", "-", "MIME type da imagem (ex.: image/jpeg, image/png)."],
        ["lngTamanhoBytes", "BIGINT (Long)", "Não", "-", "Tamanho real em bytes calculado a partir do payload Base64."],
        ["strDadosBase64", "LONGTEXT (String)", "Não", "-", "Conteúdo binário da imagem codificado em Base64."],
        ["dtmCriacao", "DATETIME(6) (LocalDateTime)", "Não", "-", "Data e hora de persistência da imagem."],
        ["blnExcluido", "TINYINT(1) (Boolean)", "Não", "IDX", "Flag de exclusão lógica (false/0 = Ativo, true/1 = Excluído logicamente)."],
        ["dtmExclusao", "DATETIME(6) (LocalDateTime)", "Sim", "-", "Data e hora da exclusão lógica da imagem."]
    ]
    body.append(w_table(["Atributo (Java / BD)", "Tipo (MySQL / Java)", "Nulo?", "Restrição", "Descrição"], img_cols, header_bg="7C3AED"))

    body.append(w_p("4.3. Tabela: CategoriaProblema", style="Heading2", bold=True, color="1E40AF", size_half_pts=23, spacing_after=80))
    body.append(w_p("Armazena o catálogo de categorias de problemas de zeladoria urbana."))
    cat_cols = [
        ["lngId", "BIGINT (Long)", "Não", "PK", "Identificador único gerado pelo JPA via JpaSequences (seqCategoriaProblema)."],
        ["strNome", "VARCHAR(100) (String)", "Não", "UQ", "Nome único da categoria (ex.: Buraco em Logradouro, Iluminação Pública)."],
        ["strDescricao", "VARCHAR(255) (String)", "Sim", "-", "Explicação detalhada dos problemas abrangidos pela categoria."],
        ["dtmCriacao", "DATETIME(6) (LocalDateTime)", "Não", "-", "Data e hora de criação da categoria."],
        ["blnExcluido", "TINYINT(1) (Boolean)", "Não", "IDX", "Flag de exclusão lógica (false/0 = Ativo, true/1 = Excluído logicamente)."],
        ["dtmExclusao", "DATETIME(6) (LocalDateTime)", "Sim", "-", "Data e hora da exclusão lógica da categoria."]
    ]
    body.append(w_table(["Atributo (Java / BD)", "Tipo (MySQL / Java)", "Nulo?", "Restrição", "Descrição"], cat_cols, header_bg="1E40AF"))

    body.append(w_p("4.4. Tabela: JpaSequences (Controle de Auto-Incremento pelo JPA)", style="Heading2", bold=True, color="334155", size_half_pts=23, spacing_after=80))
    body.append(w_p("Tabela utilizada pelo @TableGenerator do Hibernate/JPA para controlar a geração sequencial de IDs de forma independente do banco de dados."))
    seq_cols = [
        ["strSequenceName", "VARCHAR(255) (String)", "Não", "PK", "Nome da sequência controlada pelo JPA (seqCategoriaProblema, seqImagemSolicitacao, seqSolicitacaoZeladoria)."],
        ["lngNextVal", "BIGINT (Long)", "Sim", "-", "Próximo valor numérico a ser atribuído pelo JPA na criação de novos registros."]
    ]
    body.append(w_table(["Atributo (Java / BD)", "Tipo (MySQL / Java)", "Nulo?", "Restrição", "Descrição"], seq_cols, header_bg="334155"))

    # 5. Catálogo de Endpoints REST (Swagger & API Blueprint)
    body.append(w_p("5. Documentação dos Endpoints RESTful (Swagger & API Blueprint)", style="Heading1", bold=True, color="1E3A8A", size_half_pts=28, spacing_after=120))
    body.append(w_p(
        "Todos os endpoints abaixo estão documentados tanto via anotações OpenAPI 3 / Swagger UI (acessível em /swagger-ui.html e exportado em docs/swagger/openapi.json) quanto em formato API Blueprint 1A (arquivo docs/blueprint/api.apib)."
    ))
    endpoints_rows = [
        ["GET", "/api/v1/categorias", "200 OK", "Lista todas as categorias de problemas ativas (blnExcluido = false)."],
        ["GET", "/api/v1/categorias/{lngId}", "200 OK / 404", "Busca uma categoria ativa pelo seu identificador (lngId)."],
        ["POST", "/api/v1/categorias", "201 Created / 400 / 422", "Cria uma nova categoria de problema com lngId gerado pelo JPA."],
        ["PUT", "/api/v1/categorias/{lngId}", "200 OK / 400 / 404 / 422", "Atualiza strNome e strDescricao de uma categoria existente."],
        ["DELETE", "/api/v1/categorias/{lngId}", "204 No Content / 404", "Realiza a exclusão lógica (blnExcluido = true, dtmExclusao) da categoria."],
        ["GET", "/api/v1/solicitacoes", "200 OK", "Lista solicitações ativas de forma paginada, com filtros opcionais por strStatus e lngCategoriaId."],
        ["GET", "/api/v1/solicitacoes/{lngId}", "200 OK / 404", "Busca os detalhes completos de uma solicitação ativa e metadados da imagem."],
        ["GET", "/api/v1/solicitacoes/{lngId}/imagem", "200 OK / 404", "Recupera os metadados e o conteúdo strDadosBase64 da imagem armazenada na tabela separada ImagemSolicitacao."],
        ["POST", "/api/v1/solicitacoes", "201 Created / 400 / 404 / 422", "Registra uma nova solicitação de zeladoria (strTelefoneUsuario, strEmailUsuario, dblLatitude, dblLongitude, lngCategoriaId, strDescricao e imagem)."],
        ["PATCH", "/api/v1/solicitacoes/{lngId}/status", "200 OK / 400 / 404 / 422", "Atualiza o status de atendimento (strStatus) da solicitação (ex.: EM_ANALISE, EM_ANDAMENTO, CONCLUIDA)."],
        ["DELETE", "/api/v1/solicitacoes/{lngId}", "204 No Content / 404", "Realiza a exclusão lógica da solicitação e de sua respectiva imagem na tabela separada."]
    ]
    body.append(w_table(["Método HTTP", "URI do Endpoint", "Códigos HTTP", "Resumo da Operação"], endpoints_rows))

    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <w:body>
    {''.join(body)}
    <w:sectPr>
      <w:pgSz w:w="11906" w:h="16838"/>
      <w:pgMar w:top="1134" w:right="1134" w:bottom="1134" w:left="1134" w:header="708" w:footer="708" w:gutter="0"/>
    </w:sectPr>
  </w:body>
</w:document>
"""


CONTENT_TYPES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="png" ContentType="image/png"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>
"""

RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>
"""

DOCUMENT_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rIdStyles" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rIdDerImage" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/der_zeladoria.png"/>
</Relationships>
"""

STYLES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Calibri"/>
        <w:sz w:val="21"/>
        <w:szCs w:val="21"/>
        <w:lang w:val="pt-BR"/>
      </w:rPr>
    </w:rPrDefault>
  </w:docDefaults>
  <w:style w:type="paragraph" w:styleId="Title">
    <w:name w:val="Title"/>
    <w:pPr><w:spacing w:before="120" w:after="80"/></w:pPr>
    <w:rPr><w:b/><w:sz w:val="36"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/>
    <w:pPr><w:spacing w:before="240" w:after="120"/></w:pPr>
    <w:rPr><w:b/><w:color w:val="1E3A8A"/><w:sz w:val="28"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="heading 2"/>
    <w:pPr><w:spacing w:before="180" w:after="80"/></w:pPr>
    <w:rPr><w:b/><w:sz w:val="23"/></w:rPr>
  </w:style>
</w:styles>
"""


def main():
    os.makedirs(os.path.dirname(OUTPUT_DOCX), exist_ok=True)
    img_w, img_h = get_png_dimensions(DER_PNG_PATH)
    doc_xml = build_document_xml(img_w, img_h)

    with zipfile.ZipFile(OUTPUT_DOCX, "w", compression=zipfile.ZIP_DEFLATED) as docx_zip:
        docx_zip.writestr("[Content_Types].xml", CONTENT_TYPES_XML.strip())
        docx_zip.writestr("_rels/.rels", RELS_XML.strip())
        docx_zip.writestr("word/_rels/document.xml.rels", DOCUMENT_RELS_XML.strip())
        docx_zip.writestr("word/styles.xml", STYLES_XML.strip())
        docx_zip.writestr("word/document.xml", doc_xml.strip())
        docx_zip.write(DER_PNG_PATH, "word/media/der_zeladoria.png")

    print(f"Documento gerado com sucesso em: {OUTPUT_DOCX} ({os.path.getsize(OUTPUT_DOCX)} bytes)")


if __name__ == "__main__":
    main()
