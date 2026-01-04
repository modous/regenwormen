/**
 * ErrorHandlingForm Component
 *
 * Allows users to submit bug reports with optional screenshots and game state information.
 * Features include:
 * - Form validation for required fields
 * - Automatic screenshot capture using html2canvas
 * - Manual file upload with size validation (max 2MB)
 * - Real-time feedback (loading, success, error states)
 * - Multipart form data submission to backend
 */
<script setup xmlns="http://www.w3.org/1999/html">
import {ref, computed, nextTick} from "vue";
import html2canvas from "html2canvas";

const MAX_FILE_SIZE = 2 * 1024 * 1024; //2MB

const user = JSON.parse(localStorage.getItem("user"));
const gameId = localStorage.getItem("gameId");

const topic = ref("");
const category = ref("");
const priority = ref("low");
const errorDescription = ref("");
const snapshotFile = ref(null);
const fileError = ref("");
const snapshotInput = ref(null);
const submitStatus = ref('idle');
const submitMessage = ref('');

const hasSnapshot = computed(() => !!snapshotFile.value);

const props = defineProps({
  visible: { type: Boolean, required: true },
});

const emits = defineEmits(["close", "open"]);

function close() {
  emits("close");
}

function onSnapshotChange(e) {
  const files = e.target.files;
  snapshotFile.value = files && files.length > 0 ? files[0] : null;
  fileError.value = "";
  fileCheck(snapshotFile.value);
  console.log("Manual file chosen:", snapshotFile.value);
}

async function takeSnapshot() {
  try {
    //1. sluiten
    close()
    await nextTick()
    // 2. snapshot maken
    const canvas = await html2canvas(document.body);

    //3. omzetten naar file
    const dataURL = canvas.toDataURL("image/png")
    const blob = await (await fetch (dataURL)).blob()
    snapshotFile.value = new File([blob], "snapshot.png", {type: "image/png"});
    console.log("Snapshot taken:", snapshotFile);
    await fileCheck(snapshotFile.value);

  } catch (e) {
    console.error("Error taking snapshot:", e);
  } finally {
    emits("open")
    await nextTick()
    if(!snapshotFile.value){ console.log("No snapshot file found...");
      return;
    }

    // 5. File in <input type="file"> zetten
    const dt = new DataTransfer();
    dt.items.add(snapshotFile.value);
    snapshotInput.value.files = dt.files;
  }
}

async function fileCheck(file) {
  if (!file) return true;
  if (file.size > MAX_FILE_SIZE) {
    fileError.value = "File size exceeds the maximum limit of 2MB.";
    snapshotFile.value = null;
    return false;
  }
  fileError.value = "";
  return true;
}

async function submitErrorReport() {
  if (!topic.value || !category.value || !errorDescription.value) {
    alert("Please fill in all required fields!");
    return;
  }

  try {
    submitStatus.value = 'loading';
    submitMessage.value = '';

    const errorReportData = {
      name: "Game Error Report",
      topic: topic.value,
      category: category.value,
      priority: priority.value,
      details: errorDescription.value,
      userId: user?.id || user?.userId,
      gameId: gameId,
    };

    console.log("Submitting error report:", errorReportData);

    // Bouw FormData voor multipart upload
    const formData = new FormData();
    formData.append("errorReport", JSON.stringify(errorReportData));

    if (snapshotFile.value) {
      formData.append("snapshot", snapshotFile.value);
    }

    // Stuur naar backend
    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/error-reports`, {
      method: "POST",
      body: formData,
    });

    if (response.ok) {
      const result = await response.json();
      console.log("Error report submitted successfully:", result);

      // show success feedback for 2s, then reset and close
      submitStatus.value = 'success';
      submitMessage.value = 'Report submitted';

      setTimeout(() => {
        resetForm();
        submitStatus.value = 'idle';
        submitMessage.value = '';
        close();
      }, 2000);

    } else {
      const errorText = await response.text();
      console.error("Failed to submit error report:", response.status, errorText);

      // show error feedback for 2s
      submitStatus.value = 'error';
      submitMessage.value = 'Failed to submit report';

      setTimeout(() => {
        submitStatus.value = 'idle';
        submitMessage.value = '';
      }, 2000);

      alert("❌ Failed to submit error report. Try again.");
    }
  } catch (error) {
    console.error("Error submitting error report:", error);

    // show error feedback for 2s
    submitStatus.value = 'error';
    submitMessage.value = 'Error submitting report';

    setTimeout(() => {
      submitStatus.value = 'idle';
      submitMessage.value = '';
    }, 2000);

    alert("❌ Error submitting report: " + error.message);
  }
}

function resetForm() {
  topic.value = "";
  category.value = "";
  priority.value = "low";
  errorDescription.value = "";
  snapshotFile.value = null;
  if (snapshotInput.value) {
    snapshotInput.value.value = "";
  }
  fileError.value = "";
}


</script>

<template>
  <div v-if="visible" class="error-report">
    <div class="error-form">
      <h2 class="title">Error Report</h2>
      <button id="close-button" @click="close">✖</button>

      <!-- Feedback banner -->
      <div v-if="submitStatus !== 'idle'" :class="['feedback', submitStatus === 'success' ? 'feedback--success' : submitStatus === 'error' ? 'feedback--error' : 'feedback--loading']">
        <span v-if="submitStatus === 'success'">✔️ {{ submitMessage || 'Submitted' }}</span>
        <span v-else-if="submitStatus === 'error'">❌ {{ submitMessage || 'Failed' }}</span>
        <span v-else>⏳ Sending...</span>
      </div>

      <form @submit.prevent="submitErrorReport">
        <h3>Report an Issue</h3>
        <h3>{{ user.username }} - {{ gameId }}</h3>
        <div id="error-topic">
          <label for="topic">Topic:</label>
          <input type="text" id="topic" name="topic" placeholder="Briefly summarize the error" required maxlength="100" v-model="topic" :disabled="submitStatus === 'loading'"/>
          <div class="row-2col">
            <div class="field-block">
              <label for="category">Category:</label>
              <select id="category" v-model="category" required :disabled="submitStatus === 'loading'">
                <option value="" disabled>Select category</option>
                <option value="functionality_issue">Functionality Issue</option>
                <option value="ui_bug">UI Bug</option>
                <option value="performance_problem">Performance Problem</option>
                <option value="account">Account</option>
                <option value="suggestion">Suggestion</option>
                <option value="other">Other</option>
              </select>
            </div>

            <div class="field-block">
              <label for="priority">Priority:</label>
              <select id="priority" name="priority" v-model="priority" required :disabled="submitStatus === 'loading'">
                <option value="low">Low</option>
                <option value="medium">Medium</option>
                <option value="high">High</option>
              </select>
            </div>
          </div>

        </div>
        <div id="error-details">
          <label for="error-description">Describe the issue:</label>
          <textarea id="error-description" v-model="errorDescription" rows="6" cols="40" maxlength="1000"
                    placeholder="Please provide details about the error. What happened and when did this occur?" required :disabled="submitStatus === 'loading'"></textarea>
        </div>
        <div class="field-block">
          <label for="snapshot">Snapshot (helpful):</label>
          <div class="row-2col">
            <input type="file" id="snapshot" ref="snapshotInput" name="snapshot" accept="image/png, image/jpeg" @change="onSnapshotChange" :disabled="submitStatus === 'loading'"/>
            <p v-if="hasSnapshot">✔️ Screenshot captured!</p>
            <p v-else>❌ No screenshot</p>
          </div>
          <p v-if="fileError" style="color: red;">{{ fileError }}</p>
        </div>
        <div id="action-buttons">
          <button type="button" id="auto-snapshot" @click="takeSnapshot" :disabled="submitStatus === 'loading'">📸 Take snapshot</button>
          <button type="submit" :disabled="submitStatus === 'loading'">Submit Report</button>
        </div>
      </form>
    </div>
  </div>
</template>


<style scoped>
.error-report {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(15, 23, 42, 0.45); /* iets donkerder overlay */
  backdrop-filter: blur(2px);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1rem;
  z-index: 1000;
}

/* De kaart / popup */
.error-form {
  position: relative;
  background: #ffffff;
  padding: 1.75rem 1.75rem 1.5rem;
  border-radius: 16px;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.35);
  max-width: 480px;
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #e5e7eb;
}

/* Feedback banner styles */
.feedback {
  width: 100%;
  padding: 0.6rem 0.9rem;
  border-radius: 8px;
  margin-bottom: 0.75rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  box-sizing: border-box;
}
.feedback--success {
  background: #ecfdf5; /* light green */
  color: #065f46; /* dark green */
  border: 1px solid #10b98133;
}
.feedback--error {
  background: #fef2f2; /* light red */
  color: #991b1b; /* dark red */
  border: 1px solid #ef444433;
}
.feedback--loading {
  background: #eff6ff; /* light blue */
  color: #1e40af; /* dark blue */
  border: 1px solid #3b82f633;
}


/* Titel */
.title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 0.75rem;
}

/* Subtekst / username regel */
.error-form h3 {
  font-size: 0.9rem;
  font-weight: 500;
  color: #4b5563;
  margin-bottom: 0.75rem;
}

/* Form layout */
form {
  display: flex;
  flex-direction: column;
}

/* Groepen */
#error-topic,
#error-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}
/* 2 kolommen naast elkaar */
.row-2col {
  display: flex;
  gap: 1rem;
  width: 100%;
}

/* Elk veld neemt gelijk deel van de rij */
.field-block {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

/* Responsiveness: onder elkaar op kleine schermen */
@media (max-width: 480px) {
  .row-2col {
    flex-direction: column;
  }
}


/* Labels */
label {
  font-size: 0.85rem;
  font-weight: 500;
  color: #374151;
}

/* Inputs, select en textarea */
input[type="text"],
select,
textarea {
  width: 100%;
  padding: 0.6rem 0.75rem;
  border-radius: 0.5rem;
  border: 1px solid #d1d5db;
  font-size: 0.9rem;
  font-family: inherit;
  box-sizing: border-box;
  background-color: #f9fafb;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background-color 0.15s ease;
}

/* Placeholder iets lichter */
input::placeholder,
textarea::placeholder {
  color: #9ca3af;
}

/* Focus states */
input:focus,
select:focus,
textarea:focus {
  outline: none;
  border-color: #2563eb;
  background-color: #ffffff;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.18);
}

/* Textarea specifiek */
textarea {
  resize: vertical;
  min-height: 120px;
}

/* knop */
button {
  margin-top: 0.75rem;
  align-self: flex-end;
  padding: 0.6rem 1.4rem;
  border-radius: 9999px;
  border: none;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 8px 16px rgba(37, 99, 235, 0.35);
  transition: transform 0.1s ease, box-shadow 0.1s ease, background 0.15s ease;
}

button:hover, #auto-snapshot:hover{
  transform: translateY(-1px);
  background: #334155;
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.45);
}

button:active {
  transform: translateY(0);
  box-shadow: 0 5px 12px rgba(37, 99, 235, 0.35);
}

#action-buttons {
  display: flex;
  justify-content: space-between;
  width: 100%;
  gap: 1rem;
  margin-top: 1.2rem;
}

/* Screenshot-knop styling */
#auto-snapshot {
  padding: 0.6rem 1rem;
  color: white;
  font-size: 0.85rem;
  cursor: pointer;
}

#auto-snapshot:active {
  transform: translateY(0);
  box-shadow: 0 4px 10px rgba(0,0,0,0.2);
}

/* Responsive fallback */
@media (max-width: 500px) {
  #action-buttons {
    flex-direction: column;
  }
}


/* Close-knop */
#close-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 9999px;
  width: 30px;
  height: 30px;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4b5563;
  transition: background-color 0.15s ease, transform 0.1s ease, box-shadow 0.15s ease;
}

#close-button:hover {
  background: #334155;
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.3);
}

#close-button:active {
  transform: translateY(0);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.25);
}

/* Responsive tweaks */
@media (max-width: 480px) {
  .error-form {
    padding: 1.25rem 1.25rem 1rem;
    border-radius: 14px;
  }

  .title {
    font-size: 1.1rem;
  }

  button[type="submit"] {
    width: 100%;
    justify-content: center;
  }
}
</style>
