
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
import torch

MODEL_NAME = "google/flan-t5-base"

print("Loading CogniVault Transformer Summarizer...")

tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
model = AutoModelForSeq2SeqLM.from_pretrained(MODEL_NAME)
model.eval()

print("Transformer Summarizer is ready!")

app = FastAPI(title="CogniVault Transformer Memory Service")


class SummaryRequest(BaseModel):
    text: str


class SummaryResponse(BaseModel):
    summary: str


def summarize_memory(text: str) -> str:
    """Generate a concise memory summary using a pretrained Transformer."""

    if not text or not text.strip():
        return ""

    prompt = (
        "Create a concise memory note from the information below. "
        "Preserve important project facts, technologies, completed work, "
        "and next tasks. Combine related facts into short sentences. "
        "Use no more than 3 sentences. Do not invent information.\n\n"
        f"Information:\n{text}\n\n"
        "Concise memory note:"
    )

    inputs = tokenizer(
        prompt,
        return_tensors="pt",
        truncation=True,
        max_length=512
    )

    with torch.inference_mode():
        output = model.generate(
            **inputs,
            max_new_tokens=100,
            num_beams=2,
            do_sample=False
        )

    summary = tokenizer.decode(
        output[0],
        skip_special_tokens=True
    ).strip()

    return " ".join(summary.split())


@app.post("/summarize", response_model=SummaryResponse)
def summarize(request: SummaryRequest):
    if not request.text.strip():
        raise HTTPException(
            status_code=400,
            detail="Text must not be empty."
        )

    if len(request.text) > 20000:
        raise HTTPException(
            status_code=413,
            detail="Text must not exceed 20000 characters."
        )

    summary = summarize_memory(request.text)

    if not summary:
        raise HTTPException(
            status_code=500,
            detail="The summarizer could not generate a summary."
        )

    return SummaryResponse(summary=summary)


@app.get("/health")
def health():
    return {"status": "ready", "model": MODEL_NAME}


if __name__ == "__main__":
    sample = (
        "CogniVault uses Spring Boot and PostgreSQL. "
        "It uses Ollama for local language model inference. "
        "The next task is implementing persistent memory."
    )

    print("\nGenerated memory:")
    print(summarize_memory(sample))
