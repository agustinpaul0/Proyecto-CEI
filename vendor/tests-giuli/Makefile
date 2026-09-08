SRC_DIR  := src/main
OUT_DIR  := out/manual
JAR_FILE := out/minijava.jar
MAIN_CLS := main.ModuloPrincipalParser

SRCS     := $(shell find $(SRC_DIR) -name "*.java")

# --- Fuzz test case generation ----------------------------------------
ANTLR_GRAMMAR      := tools/grammarinator/grammar/MiniJava.g4
JAVACC_GRAMMAR     := tools/grammarinator/grammar/MiniJavaParser.jj
TEST_GENERATOR     := tools/grammarinator/generate_fuzz_cases.py
GRAMMAR_GENERATOR  := tools/grammar_from_spec/grammar_from_spec.py
GRAMMAR_SPEC       := tools/grammar_from_spec/minijava_spec.py
TRANSFORMER        := tools/grammarinator/avoid_keyword_identifiers.py
NUM_POSITIVE       ?= 1000
NUM_NEGATIVE       ?= 1000
MAX_DEPTH          ?= 50
FUZZ_WORK_DIR      ?= tools/grammarinator/out
FUZZ_SEED_FILE     := tools/grammarinator/fuzz_seed.txt
FUZZ_STAMP         := resources/parser/.fuzz-ready
ANTLR_FILE         := tools/jars/antlr.jar
JAVACC_FILE        := tools/jars/javacc.jar

# --- Fuzzer tooling venv ----------------------------------------------
VENV                 := .venv
VENV_BIN             := $(VENV)/bin
GRAMMARINATOR_VERSION?= 26.1
VENV_READY           := $(VENV_BIN)/grammarinator-generate

.PHONY: all build clean venv grammar fuzz fresh-fuzz

all: build

build: $(JAR_FILE)

$(JAR_FILE): $(SRCS)
	@echo "Source files changed. Compiling and packaging..."
	@mkdir -p $(OUT_DIR)
	javac -d $(OUT_DIR) $(SRCS)
	jar cfe $(JAR_FILE) $(MAIN_CLS) -C $(OUT_DIR) .

clean:
	rm -rf $(OUT_DIR) $(JAR_FILE)
	rm -rf $(FUZZ_WORK_DIR)
	rm -f $(FUZZ_STAMP)

$(VENV_READY):
	python3 -m venv $(VENV)
	$(VENV_BIN)/pip install --upgrade pip
	$(VENV_BIN)/pip install grammarinator==$(GRAMMARINATOR_VERSION)

venv: $(VENV_READY)

grammar: $(ANTLR_GRAMMAR) $(JAVACC_GRAMMAR)

$(ANTLR_GRAMMAR) $(JAVACC_GRAMMAR): $(GRAMMAR_SPEC) $(GRAMMAR_GENERATOR) $(VENV_BIN)
	PATH="$(VENV_BIN):$$PATH" $(VENV_BIN)/python3 $(GRAMMAR_GENERATOR)

$(FUZZ_STAMP): $(ANTLR_GRAMMAR) $(JAVACC_GRAMMAR) $(TEST_GENERATOR) $(TRANSFORMER) $(VENV_READY) $(FUZZ_SEED_FILE)
	@rm -rf $(FUZZ_WORK_DIR)
	@if [ ! -s "$(FUZZ_SEED_FILE)" ]; then \
		echo "Error: $(FUZZ_SEED_FILE) is empty."; \
		echo "Run 'make fresh-fuzz' to generate a new seed and fuzz corpus."; \
		exit 1; \
	fi
	PATH="$(VENV_BIN):$$PATH" $(VENV_BIN)/python3 $(TEST_GENERATOR) \
		--num-positive $(NUM_POSITIVE) \
		--num-negative $(NUM_NEGATIVE) \
		--max-depth $(MAX_DEPTH) \
		--antlr-jar $(ANTLR_FILE) \
		--javacc-jar $(JAVACC_FILE) \
		--seed "$$(cat $(FUZZ_SEED_FILE))"
	@touch $(FUZZ_STAMP)

fuzz: $(FUZZ_STAMP)

fresh-fuzz: $(ANTLR_GRAMMAR) $(JAVACC_GRAMMAR) $(TEST_GENERATOR) $(TRANSFORMER) $(VENV_READY)
	@rm -rf $(FUZZ_WORK_DIR)
	PATH="$(VENV_BIN):$$PATH" $(VENV_BIN)/python3 $(TEST_GENERATOR) \
		--num-positive $(NUM_POSITIVE) \
		--num-negative $(NUM_NEGATIVE) \
		--max-depth $(MAX_DEPTH) \
		--antlr-jar $(ANTLR_FILE) \
		--javacc-jar $(JAVACC_FILE)
	@touch $(FUZZ_STAMP)
