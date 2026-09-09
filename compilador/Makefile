# Generación del corpus de fuzzing del analizador sintáctico.
#
# El corpus se genera OFFLINE y se versiona: `mvn verify` corre los casos ya
# generados y no necesita ni el venv ni los jars. Esto es sólo para regenerarlo.
#
#     make venv    # instala Grammarinator en .venv/
#     make fuzz    # regenera el corpus con la semilla versionada
#     make limpiar

VENV        := .venv
PIP         := $(VENV)/bin/pip
GRAMMARINATOR_VERSION ?= 26.1
LISTO       := $(VENV)/bin/grammarinator-generate

JARS        := herramientas/fuzz/jars
JAVACC      := $(JARS)/javacc.jar
ANTLR       := $(JARS)/antlr.jar
JAVACC_URL  := https://repo1.maven.org/maven2/net/java/dev/javacc/javacc/7.0.13/javacc-7.0.13.jar
ANTLR_URL   := https://repo1.maven.org/maven2/org/antlr/antlr4/4.13.2/antlr4-4.13.2-complete.jar

POSITIVOS   ?= 700
NEGATIVOS   ?= 700
SEMILLA     ?= 20260923

.PHONY: venv jars fuzz limpiar

$(LISTO):
	python3 -m venv $(VENV)
	$(PIP) install --quiet --upgrade pip
	$(PIP) install --quiet grammarinator==$(GRAMMARINATOR_VERSION)

venv: $(LISTO)

$(JAVACC):
	@mkdir -p $(JARS)
	curl -sSL -o $@ $(JAVACC_URL)

$(ANTLR):
	@mkdir -p $(JARS)
	curl -sSL -o $@ $(ANTLR_URL)

jars: $(JAVACC) $(ANTLR)

fuzz: $(LISTO) jars
	python3 herramientas/generar_corpus_fuzz.py \
		--positivos $(POSITIVOS) --negativos $(NEGATIVOS) --semilla $(SEMILLA) --limpiar

limpiar:
	rm -rf herramientas/fuzz/trabajo
