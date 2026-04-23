import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './modules/ChatBot.module.css';
import { useAuth } from '../contexts/AuthContext';
import NavigationBar from '../components/NavigationBar';

// Markdown parser
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

const ChatBot = () => {
    const { orderId } = useParams();
    const navigate = useNavigate();
    const [messages, setMessages] = useState([]);
    const [inputMessage, setInputMessage] = useState('');
    const [isTyping, setIsTyping] = useState(false);
    const messagesEndRef = useRef(null);
    const { token } = useAuth();

    const suggestionChips = [
        "Tell me about your products",
        "What are your prices?",
        "How can I place an order?",
        "Contact support"
    ];

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    useEffect(() => {
        // Remove the parseInt validation since orderNumber is a string
        if (!orderId) {
            console.error('No order ID provided');
            navigate('/dashboard');
            return;
        }

        // Initial greeting with the order number
        setMessages([{
            type: 'bot',
            content: `Hello! I'm Orbi, your AI Sales Assistant. I'm here to help with Order ${orderId}`,
            timestamp: new Date().toLocaleTimeString()
        }]);
    }, [orderId, navigate]);

    const handleChipClick = (suggestion) => {
        setInputMessage(suggestion);
    };

    const sendMessage = async (e) => {
        e.preventDefault();
        if (!inputMessage.trim()) return;

        if (!orderId) {
            console.error('No order ID available');
            return;
        }

        const newMessage = {
            type: 'user',
            content: inputMessage,
            timestamp: new Date().toLocaleTimeString()
        };

        setMessages(prev => [...prev, newMessage]);
        setInputMessage('');
        setIsTyping(true);

        try {
            const response = await fetch(`http://localhost:8080/api/chat/${orderId}/ask`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    question: inputMessage
                })
            });

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();

            setTimeout(() => {
                setIsTyping(false);
                setMessages(prev => [...prev, {
                    type: 'bot',
                    content: data.response || data.answer || data,
                    timestamp: new Date().toLocaleTimeString()
                }]);
            }, 1000);

        } catch (error) {
            console.error('Error:', error);
            setIsTyping(false);
            setMessages(prev => [...prev, {
                type: 'bot',
                content: 'Sorry, I encountered an error. Please try again.',
                timestamp: new Date().toLocaleTimeString()
            }]);
        }
    };

    // 🔹 Renders structured JSON if backend returns JSON
    const formatJsonContent = (content) => {
        if (Array.isArray(content.strategies)) {
            return (
                <div>
                    {content.strategies.map((s, i) => (
                        <div key={i} className={styles.transcriptSection}>
                            <h3>{s.title}</h3>
                            <p><strong>Objective:</strong> {s.objective}</p>
                            <p><strong>Action:</strong> {s.action}</p>
                            <ul>
                                {s.keyPoints.map((kp, j) => <li key={j}>{kp}</li>)}
                            </ul>
                            <p><strong>Priority:</strong> {s.priority}</p>
                        </div>
                    ))}
                </div>
            );
        }

        // fallback for generic JSON objects
        return (
            <div className={styles.responseWrapper}>
                {Object.entries(content).map(([key, value]) => (
                    <div key={key} className={styles.transcriptSection}>
                        <h3>{key}</h3>
                        <p>{JSON.stringify(value)}</p>
                    </div>
                ))}
            </div>
        );
    };

    // 🔹 Handles strings, markdown, or JSON
    const formatMessage = (content) => {
        if (typeof content === 'string') {
            try {
                const jsonContent = JSON.parse(content);
                return formatJsonContent(jsonContent);
            } catch {
                // not JSON → treat as markdown
                return (
                    <ReactMarkdown
                        remarkPlugins={[remarkGfm]}
                        components={{
                            strong: ({ children }) => <strong className={styles.emphasis}>{children}</strong>,
                            li: ({ children }) => <li className={styles.bulletItem}>{children}</li>,
                            ul: ({ children }) => <ul className={styles.bulletList}>{children}</ul>,
                            ol: ({ children }) => <ol className={styles.orderedList}>{children}</ol>,
                            p: ({ children }) => <p>{children}</p>
                        }}
                    >
                        {content}
                    </ReactMarkdown>
                );
            }
        }
        if (typeof content === 'object') {
            return formatJsonContent(content);
        }
        return content;
    };

    return (
        <div className="min-h-screen bg-background">
            <NavigationBar />
            <div className={styles.chatbot}>
            <div className={styles.chatHeader}>
                <h3>Orbi AI Sales Assistant</h3>
                <div className={styles.statusIndicator}>
                    <div className={styles.statusDot}></div>
                    <span>Online</span>
                </div>
            </div>

            <div className={styles.chatMessages}>
                {messages.map((message, index) => (
                    <div key={index}
                        className={`${styles.message} ${message.type === 'user' ? styles.userMessage : styles.botMessage}`}
                    >
                        {formatMessage(message.content)}
                        <div className={styles.timestamp}>{message.timestamp}</div>
                    </div>
                ))}
                {isTyping && (
                    <div className={styles.typing}>
                        <div className={styles.typingDot}></div>
                        <div className={styles.typingDot}></div>
                        <div className={styles.typingDot}></div>
                    </div>
                )}
                <div ref={messagesEndRef} />
            </div>

            <div className={styles.suggestionChips}>
                {suggestionChips.map((chip, index) => (
                    <div
                        key={index}
                        className={styles.chip}
                        onClick={() => handleChipClick(chip)}
                    >
                        {chip}
                    </div>
                ))}
            </div>

            <form onSubmit={sendMessage} className={styles.chatInput}>
                <input
                    type="text"
                    value={inputMessage}
                    onChange={(e) => setInputMessage(e.target.value)}
                    placeholder="Type your message..."
                />
                <button type="submit">Send</button>
            </form>
            </div>
        </div>
    );
};

export default ChatBot;
