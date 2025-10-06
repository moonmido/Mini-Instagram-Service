# Mini Instagram Demo

## Overview
Mini Instagram Demo is a simplified photo-sharing platform built with **Spring Boot**.  
It allows users to:

- Upload, view, and download photos.
- Follow/unfollow other users.
- Get a personalized News Feed containing top photos from followed users.

This project focuses on core functionality and simplicity.

---

## Features

### Functional Requirements
1. Upload/download/view photos (`PhotoService`).
2. Follow/unfollow users (`UserFollowService`).
3. Personalized News Feed for each user (`NewsFeedService`).
4. Real-time follower/following count updates.

### Non-Functional Requirements
- Basic reliability for uploaded photos.
- Separation of read/write operations for better performance.
