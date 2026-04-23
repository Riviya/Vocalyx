import React, { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Badge } from '@/components/ui/badge';
import { 
  Upload, 
  FileAudio, 
  CheckCircle, 
  AlertCircle, 
  X,
  File,
  Download
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

interface UploadedFile {
  file: File;
  filePath: string;
  fileSize: number;
  contentType: string;
}

interface FileUploadComponentProps {
  onFileUpload: (file: UploadedFile) => void;
  uploadedFile: UploadedFile | null;
}

const FileUploadComponent: React.FC<FileUploadComponentProps> = ({
  onFileUpload,
  uploadedFile
}) => {
  const { toast } = useToast();
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);

  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    const file = acceptedFiles[0];
    if (!file) return;

    // Validate file type
    const validTypes = ['audio/mpeg', 'audio/wav', 'audio/mp4', 'audio/m4a', 'audio/x-m4a'];
    if (!validTypes.includes(file.type)) {
      toast({
        title: "Invalid File Type",
        description: "Please upload an audio file (MP3, WAV, M4A)",
        variant: "destructive",
      });
      return;
    }

    // Validate file size (50MB limit)
    const maxSize = 50 * 1024 * 1024; // 50MB
    if (file.size > maxSize) {
      toast({
        title: "File Too Large",
        description: "File size must be less than 50MB",
        variant: "destructive",
      });
      return;
    }

    setIsUploading(true);
    setUploadProgress(0);

    try {
      // Simulate upload progress
      const progressInterval = setInterval(() => {
        setUploadProgress(prev => {
          if (prev >= 90) {
            clearInterval(progressInterval);
            return 90;
          }
          return prev + 10;
        });
      }, 200);

      // Create FormData for file upload
      const formData = new FormData();
      formData.append('file', file);

      // Upload file to backend
      const token = localStorage.getItem('finsight_token');
      console.log('Uploading file:', file.name, 'Type:', file.type, 'Size:', file.size);
      console.log('Token available:', !!token);
      
      const response = await fetch('http://localhost:8080/api/upload/audio', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData,
      });

      clearInterval(progressInterval);
      setUploadProgress(100);

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Upload failed:', response.status, errorText);
        throw new Error(`Upload failed: ${response.status} - ${errorText}`);
      }

      const result = await response.json();

      if (result.success) {
        const uploadedFileData: UploadedFile = {
          file,
          filePath: result.filePath,
          fileSize: result.fileSize,
          contentType: result.contentType,
        };

        onFileUpload(uploadedFileData);
        
        toast({
          title: "Upload Successful",
          description: "Audio file uploaded successfully",
        });
      } else {
        throw new Error(result.message || 'Upload failed');
      }
    } catch (error) {
      console.error('Upload error:', error);
      toast({
        title: "Upload Failed",
        description: error instanceof Error ? error.message : 'Failed to upload file',
        variant: "destructive",
      });
    } finally {
      setIsUploading(false);
      setUploadProgress(0);
    }
  }, [onFileUpload, toast]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'audio/*': ['.mp3', '.wav', '.m4a', '.mpeg', '.mp4']
    },
    multiple: false,
    disabled: isUploading || !!uploadedFile
  });

  const removeFile = () => {
    onFileUpload(null as any);
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  return (
    <div className="space-y-6">
      {!uploadedFile ? (
        <div
          {...getRootProps()}
          className={`
            border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors
            ${isDragActive 
              ? 'border-primary bg-primary/5' 
              : 'border-muted-foreground/25 hover:border-primary/50'
            }
            ${isUploading ? 'pointer-events-none opacity-50' : ''}
          `}
        >
          <input {...getInputProps()} />
          <div className="space-y-4">
            <div className="mx-auto w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center">
              {isUploading ? (
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
              ) : (
                <Upload className="h-8 w-8 text-primary" />
              )}
            </div>
            
            <div>
              <h3 className="text-lg font-semibold">
                {isUploading ? 'Uploading...' : isDragActive ? 'Drop the file here' : 'Upload Audio File'}
              </h3>
              <p className="text-muted-foreground">
                Drag and drop your call recording here, or click to browse
              </p>
            </div>

            {isUploading && (
              <div className="space-y-2">
                <Progress value={uploadProgress} className="w-full" />
                <p className="text-sm text-muted-foreground">{uploadProgress}% uploaded</p>
              </div>
            )}

            <div className="space-y-2">
              <p className="text-sm text-muted-foreground">Supported formats:</p>
              <div className="flex justify-center space-x-2">
                {['MP3', 'WAV', 'M4A'].map(format => (
                  <Badge key={format} variant="secondary">{format}</Badge>
                ))}
              </div>
              <p className="text-xs text-muted-foreground">Max file size: 50MB</p>
            </div>
          </div>
        </div>
      ) : (
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center space-x-4">
              <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                <CheckCircle className="h-6 w-6 text-green-600" />
              </div>
              <div className="flex-1">
                <div className="flex items-center space-x-2">
                  <FileAudio className="h-5 w-5 text-primary" />
                  <h3 className="font-semibold">{uploadedFile.file.name}</h3>
                </div>
                <div className="flex items-center space-x-4 mt-1">
                  <p className="text-sm text-muted-foreground">
                    {formatFileSize(uploadedFile.fileSize)}
                  </p>
                  <Badge variant="secondary">{uploadedFile.contentType}</Badge>
                </div>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={removeFile}
                className="text-muted-foreground hover:text-destructive"
              >
                <X className="h-4 w-4" />
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Instructions */}
      <Card>
        <CardContent className="pt-6">
          <h4 className="font-semibold mb-3 flex items-center space-x-2">
            <AlertCircle className="h-4 w-4" />
            <span>Upload Instructions</span>
          </h4>
          <ul className="space-y-2 text-sm text-muted-foreground">
            <li className="flex items-start space-x-2">
              <span className="text-primary">•</span>
              <span>Ensure your audio file is clear and has minimal background noise</span>
            </li>
            <li className="flex items-start space-x-2">
              <span className="text-primary">•</span>
              <span>Supported formats: MP3, WAV, M4A (up to 50MB)</span>
            </li>
            <li className="flex items-start space-x-2">
              <span className="text-primary">•</span>
              <span>For best results, use recordings with clear speech</span>
            </li>
            <li className="flex items-start space-x-2">
              <span className="text-primary">•</span>
              <span>Longer recordings may take more time to process</span>
            </li>
          </ul>
        </CardContent>
      </Card>
    </div>
  );
};

export default FileUploadComponent;
