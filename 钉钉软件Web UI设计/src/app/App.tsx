import { useState } from "react";
import { DingTalkSidebar } from "@/app/components/DingTalkSidebar";
import { MessageList } from "@/app/components/MessageList";
import { ChatArea } from "@/app/components/ChatArea";
import { WorkbenchPanel } from "@/app/components/WorkbenchPanel";
import { PhonePanel } from "@/app/components/PhonePanel";
import { ContactsPanel } from "@/app/components/ContactsPanel";
import { DocumentsPanel } from "@/app/components/DocumentsPanel";
import { CalendarPanel } from "@/app/components/CalendarPanel";
import { MorePanel } from "@/app/components/MorePanel";
import { SettingsPanel } from "@/app/components/SettingsPanel";
import { UserProfilePanel } from "@/app/components/UserProfilePanel";

export default function App() {
  const [activeTab, setActiveTab] = useState("message");
  const [selectedMessageId, setSelectedMessageId] = useState("1");

  return (
    <div className="size-full flex bg-white overflow-hidden">
      {/* Left Sidebar */}
      <DingTalkSidebar activeTab={activeTab} onTabChange={setActiveTab} />

      {/* Main Content Area */}
      {activeTab === "message" && (
        <>
          <MessageList
            selectedMessageId={selectedMessageId}
            onSelectMessage={setSelectedMessageId}
          />
          <ChatArea />
        </>
      )}

      {activeTab === "phone" && <PhonePanel />}

      {activeTab === "contacts" && <ContactsPanel />}

      {activeTab === "docs" && <DocumentsPanel />}

      {activeTab === "calendar" && <CalendarPanel />}

      {activeTab === "more" && <MorePanel />}

      {activeTab === "settings" && <SettingsPanel />}

      {activeTab === "profile" && <UserProfilePanel />}
    </div>
  );
}