import { useState, useEffect } from "react";
import "./App.css";

function App() {
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [uploading, setUploading] = useState(false);
  const [uploadMessage, setUploadMessage] = useState("");
  const [documentCount, setDocumentCount] = useState(0);
  const [chunkCount, setChunkCount] = useState(0);
  const loadCounts = async () => {
  try {
    const [documentsResponse, chunksResponse] = await Promise.all([
      fetch("http://localhost:8080/api/documents"),
      fetch("http://localhost:8080/api/documents/count"),
    ]);

    if (!documentsResponse.ok || !chunksResponse.ok) {
      throw new Error("Failed to load counts");
    }

    const chunks = await documentsResponse.json();
    const totalChunks = await chunksResponse.json();

    setChunkCount(Number(totalChunks));
    console.log("Chunks:", totalChunks, "Documents:", chunks);
    setDocumentCount(
      new Set(chunks.map((chunk) => chunk.documentName)).size
    );
  } catch (err) {
    console.error("Could not load document counts:", err);
  }
};
useEffect(() => {
  loadCounts();
}, []);
  const handleUpload = async (event) => {
  const file = event.target.files[0];

  if (!file) return;

  if (!file.name.toLowerCase().endsWith(".txt")) {
    setUploadMessage("Please select a .txt file.");
    return;
  }

  setUploading(true);
  setUploadMessage("");

  try {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(
      "http://localhost:8080/api/documents/upload",
      {
        method: "POST",
        body: formData,
      }
    );

    const result = await response.text();

    if (!response.ok) {
      throw new Error(result || "Upload failed.");
    }

    setUploadMessage(`Successfully uploaded ${file.name}!`);
    await loadCounts();
  } catch (error) {
    setUploadMessage(`Upload failed: ${error.message}`);
  } finally {
    setUploading(false);
    event.target.value = "";
  }
};

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!question.trim() || loading) return;

    setLoading(true);
    setAnswer("");
    setError("");

    try {
      const response = await fetch(
        `http://localhost:8080/api/documents/ask?q=${encodeURIComponent(
          question
        )}`
      );

      const result = await response.text();

      if (!response.ok) {
        throw new Error(result || "Failed to get an answer.");
      }

      setAnswer(result);
    } catch (err) {
      setError(
        err.message === "Failed to fetch"
          ? "Could not connect to CogniVault. Check that Spring Boot is running."
          : err.message || "Something went wrong. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      <aside className="sidebar">
        <h2>🧠 CogniVault</h2>
        <p className="subtitle">AI Document Intelligence</p>

        <nav>
          <div className="nav-item active">▦ Dashboard</div>
          <div className="nav-item">▤ Documents</div>
          <div className="nav-item">⌕ Semantic Search</div>
          <div className="nav-item">✧ AI Assistant</div>
        </nav>

        <div className="sidebar-bottom">
          <span className="status-dot"></span> Local AI · Ollama
        </div>
      </aside>

      <main className="main">
        <header className="topbar">
          <div>
            <h1>Dashboard</h1>
            <p>Your personal knowledge workspace</p>
          </div>
          <span className="badge">● System Online</span>
        </header>

        <section className="welcome">
          <p className="eyebrow">YOUR KNOWLEDGE, CONNECTED</p>
          <h2>Welcome to CogniVault</h2>
          <p>
            Upload documents, explore their meaning, and get AI-powered
            answers from your own knowledge base.
          </p>
        </section>

        <section className="stats">
          <article className="stat-card">
            <span>Documents</span>
            <h2>{documentCount}</h2>
            <p>Uploaded to your vault</p>
          </article>

          <article className="stat-card">
            <span>Text chunks</span>
            <h2>{chunkCount}</h2>
            <p>Processed for retrieval</p>
          </article>

          <article className="stat-card">
            <span>AI model</span>
            <h2 className="model">Llama 3.2</h2>
            <p>Running locally with Ollama</p>
          </article>
        </section>

        <section className="workspace">
          <article className="panel">
            <div className="panel-heading">
              <div>
                <h3>Upload documents</h3>
                <p>Add files to your knowledge base</p>
              </div>
              <span className="panel-icon">↑</span>
            </div>

            <label className="upload-area">
              <span className="upload-icon">⇧</span>
              <strong>Choose a document</strong>
              <span>TXT files supported by the current backend</span>
              <input
                type="file"
                accept=".txt,text/plain"
                onChange={handleUpload}
                disabled={uploading}
              />
            </label>

            {uploadMessage && (
              <p className={`hint ${uploadMessage.includes("failed") ? "error" : "success"}`}>
                {uploadMessage}
              </p>
            )}
          </article>

          <article className="panel">
            <div className="panel-heading">
              <div>
                <h3>Ask your documents</h3>
                <p>Explore your knowledge with AI</p>
              </div>
              <span className="panel-icon">✧</span>
            </div>

            {!answer && !loading && !error && (
              <div className="chat-placeholder">
                <div className="assistant-icon">✧</div>
                <strong>What would you like to know?</strong>
                <p>Ask a question about your uploaded documents.</p>
              </div>
            )}

            {loading && (
              <div className="chat-placeholder">
                <div className="assistant-icon">✧</div>
                <strong>Thinking...</strong>
                <p>Searching your documents and generating an answer.</p>
              </div>
            )}

            {answer && (
              <div className="answer-box" aria-live="polite">
                <strong>✧ CogniVault's Answer</strong>
                <p>{answer}</p>
              </div>
            )}

            {error && (
              <div className="answer-box error-box" role="alert">
                <strong>Something went wrong</strong>
                <p>{error}</p>
              </div>
            )}

            <form className="question-form" onSubmit={handleSubmit}>
              <input
                value={question}
                onChange={(event) => setQuestion(event.target.value)}
                placeholder="Ask something about your documents..."
                aria-label="Your question"
                disabled={loading}
              />

              <button
                type="submit"
                aria-label="Send question"
                disabled={loading || !question.trim()}
              >
                {loading ? "…" : "➤"}
              </button>
            </form>
          </article>
        </section>

        <footer>CogniVault · Built with React, Spring Boot & Ollama</footer>
      </main>
    </div>
  );
}

export default App;
